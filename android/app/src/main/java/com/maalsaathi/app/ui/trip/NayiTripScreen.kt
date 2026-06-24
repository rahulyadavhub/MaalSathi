package com.maalsaathi.app.ui.trip

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.maalsaathi.app.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NayiTripScreen(viewModel: TripViewModel, onBack: () -> Unit, onExtracted: () -> Unit) {
    val aiState by viewModel.aiState.collectAsState()
    val c = Ms.colors
    val ctx = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var attachedPhoto by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    val micPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) viewModel.startRecording() }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            // TODO: Save bitmap to temp file, get URI
            attachedPhoto = Uri.EMPTY // placeholder — photo captured
        }
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) attachedPhoto = uri
    }

    when (aiState) { is AiState.TripExtracted -> { onExtracted(); return }; else -> {} }

    // Photo picker bottom sheet
    if (showPhotoPicker) {
        ModalBottomSheet(onDismissRequest = { showPhotoPicker = false }, containerColor = c.card) {
            Column(Modifier.padding(20.dp)) {
                Text("Photo kahan se?", style = MsType.titleLarge)
                Spacer(Modifier.height(16.dp))
                MsPrimaryButton("📷 Camera se photo lo", onClick = { showPhotoPicker = false; cameraLauncher.launch(null) })
                Spacer(Modifier.height(8.dp))
                MsSecondaryButton("🖼️ Gallery se chunno", onClick = { showPhotoPicker = false; galleryLauncher.launch("image/*") })
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad)) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Text("Nayi Trip", style = MsType.headlineMedium, color = c.textPrimary)
        }
        Text("Kaise shuru karna hai?", style = MsType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(20.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Voice card
            MsHighlightCard(Modifier.padding(0.dp)) {
                Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(72.dp).background(Color(0xFFE8FAF0), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Mic, null, tint = c.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Bol ke shuru karo", style = MsType.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Kuch bhi bolo — Hindi, Hinglish,\nkoi bhi bhasha chalegi", style = MsType.bodyMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))

                    when (aiState) {
                        is AiState.Recording -> MsPrimaryButton("⏹️ Rokne ke liye dabao", onClick = { viewModel.stopRecordingForTrip() })
                        is AiState.Transcribing -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MsPrimaryButton("", onClick = {}, loading = true)
                                Spacer(Modifier.height(6.dp))
                                Text("Sun raha hoon...", style = MsType.bodyMedium.copy(color = c.primary))
                            }
                        }
                        is AiState.Extracting -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MsPrimaryButton("", onClick = {}, loading = true)
                                Spacer(Modifier.height(6.dp))
                                Text("Samajh raha hoon...", style = MsType.bodyMedium.copy(color = c.primary))
                            }
                        }
                        else -> MsPrimaryButton("🎙️ Abhi Bolo", onClick = {
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                                viewModel.startRecording()
                            else micPerm.launch(Manifest.permission.RECORD_AUDIO)
                        })
                    }
                }
            }

            // Divider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = c.divider)
                Text("  ya  ", fontSize = 16.sp, color = c.textSecondary)
                HorizontalDivider(Modifier.weight(1f), color = c.divider)
            }

            // Text card
            MsCard {
                Text("✏️ Type karke likho", style = MsType.titleLarge, color = c.textPrimary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = textInput, onValueChange = { textInput = it }, modifier = Modifier.fillMaxWidth(), minLines = 3,
                    placeholder = { Text("Mumbai se Delhi, cement 50 ton, 2 lakh bhada", color = c.textSecondary.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(Sp.inputRadius),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = c.primary, unfocusedBorderColor = c.divider, focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary, cursorColor = c.primary),
                )

                // Photo attach option
                Spacer(Modifier.height(8.dp))
                if (attachedPhoto != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(c.statBg), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, null, tint = c.primary, modifier = Modifier.size(24.dp))
                        }
                        Text("Photo attached ✓", style = MsType.labelLarge.copy(color = c.primary, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { attachedPhoto = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, "Remove", tint = c.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Row(
                        Modifier.clickable { showPhotoPicker = true }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                        Text("Receipt ya photo attach karo", style = MsType.labelLarge)
                    }
                }

                Spacer(Modifier.height(12.dp))
                MsPrimaryButton("→ Aage Badho", onClick = { if (textInput.isNotBlank()) viewModel.extractTripFromText(textInput.trim()) },
                    enabled = textInput.isNotBlank() && aiState !is AiState.Extracting)
            }

            if (aiState is AiState.Error) {
                Surface(shape = RoundedCornerShape(Sp.inputRadius), color = c.loss.copy(alpha = 0.08f)) {
                    Text((aiState as AiState.Error).message, style = MsType.bodyLarge, color = c.loss, modifier = Modifier.padding(14.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
