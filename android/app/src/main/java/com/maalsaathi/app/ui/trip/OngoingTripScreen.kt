package com.maalsaathi.app.ui.trip

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.ui.common.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingTripScreen(viewModel: TripViewModel, onBack: () -> Unit, onTripEnded: (String) -> Unit) {
    val trip by viewModel.ongoingTrip.collectAsState()
    val entries by viewModel.currentEntries.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val c = Ms.colors; val ctx = LocalContext.current
    var text by remember { mutableStateOf("") }
    var showEnd by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale("en", "IN")) }

    val t = trip ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Koi chal rahi trip nahi", style = MsType.bodyLarge, color = c.textSecondary) }; return
    }
    LaunchedEffect(t.id) { viewModel.loadEntriesForTrip(t.id) }
    LaunchedEffect(entries.size) { if (entries.isNotEmpty()) listState.animateScrollToItem(0) }

    val micPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) viewModel.startRecording() }

    // Quick confirm sheet
    if (aiState is AiState.EntryExtracted) {
        val res = (aiState as AiState.EntryExtracted).data; val raw = (aiState as AiState.EntryExtracted).rawText
        val sheet = rememberModalBottomSheetState()
        var progress by remember { mutableFloatStateOf(1f) }
        LaunchedEffect(Unit) { repeat(100) { delay(100); progress = 1f - (it + 1) / 100f }; viewModel.resetAiState() }

        ModalBottomSheet(onDismissRequest = { viewModel.resetAiState() }, sheetState = sheet, containerColor = c.card) {
            Column(Modifier.padding(20.dp)) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp), color = c.primary, trackColor = c.divider, strokeCap = StrokeCap.Round)
                Spacer(Modifier.height(16.dp))
                Text("Sahi hai?", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text("${res.emoji} ${res.category.replaceFirstChar { it.uppercase() }} — ${formatRupees(res.amount)}", style = MsType.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MsPrimaryButton("✅ Haan, Save Karo", onClick = { viewModel.confirmEntry(res, t.id, raw) }, modifier = Modifier.weight(1f))
                    MsSecondaryButton("✏️ Badlo", onClick = { viewModel.resetAiState() }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (aiState is AiState.Error) { LaunchedEffect(Unit) { delay(3000); viewModel.resetAiState() } }

    Column(Modifier.fillMaxSize().imePadding()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = Sp.screenHPad, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Column(Modifier.weight(1f)) {
                Text("${capWords(t.origin)} → ${capWords(t.destination)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary)
                val sub = buildString {
                    if (t.cargoType.isNotBlank()) append("${capWords(t.cargoType)} · ")
                    if (t.cargoWeightTons > 0) append("${formatWeight(t.cargoWeightTons)} · ")
                    append("${formatDuration(System.currentTimeMillis() - t.startTime)} se chal rahi")
                }
                Text(sub, fontSize = 13.sp, fontFamily = NotoSans, color = c.textSecondary)
            }
            StatusPill("LIVE", PillType.LIVE)
        }

        // Stats
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Bhada", formatRupees(t.freightAmount), Modifier.weight(1f), c.profit)
            StatBox("Kharcha", formatRupees(t.totalExpenses), Modifier.weight(1f), c.loss)
        }

        // Journal
        SectionHeader("Journal", Modifier.padding(horizontal = Sp.screenHPad))
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = Sp.screenHPad), reverseLayout = true) {
            items(entries.reversed(), key = { it.id }) { e ->
                AnimatedVisibility(visible = true, enter = slideInVertically { it }) {
                    JournalEntryRow(timeFmt.format(Date(e.timestamp)), e.emoji, "${e.category.replaceFirstChar { it.uppercase() }}${if (e.note.isNotBlank()) " — ${e.note}" else ""}", e.amount, e.type, isLast = e == entries.first())
                }
            }
            item {
                JournalEntryRow(timeFmt.format(Date(t.startTime)), "🚛", "Trip shuru — ${capWords(t.origin)}", 0, com.maalsaathi.app.data.models.EntryType.NOTE, isFirst = true, isLast = entries.isEmpty())
            }
        }

        if (entries.isEmpty()) {
            Text("Abhi koi entry nahi\nKharcha bolke ya likhke daalo", style = MsType.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
        }

        // Error
        if (aiState is AiState.Error) {
            Text((aiState as AiState.Error).message, fontSize = 13.sp, fontFamily = NotoSans, color = c.loss, modifier = Modifier.padding(horizontal = Sp.screenHPad, vertical = 4.dp))
        }

        // Input bar
        HorizontalDivider(color = c.border)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val isRec = aiState is AiState.Recording
            IconButton(
                onClick = {
                    if (isRec) viewModel.stopRecordingForEntry(t.id)
                    else if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) viewModel.startRecording()
                    else micPerm.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.size(Sp.micButton),
                colors = IconButtonDefaults.iconButtonColors(containerColor = if (isRec) c.loss else c.primary, contentColor = c.onPrimary),
            ) { Icon(if (isRec) Icons.Default.Stop else Icons.Default.Mic, "Voice", Modifier.size(28.dp)) }

            OutlinedTextField(
                value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), singleLine = true,
                placeholder = { Text("Kharcha bolke ya type karo...", fontSize = 16.sp, color = c.textSecondary.copy(0.5f)) },
                shape = RoundedCornerShape(Sp.inputRadius),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = c.primary, unfocusedBorderColor = c.border, focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary, cursorColor = c.primary),
            )

            IconButton(
                onClick = { if (text.isNotBlank()) { viewModel.categorizeEntryFromText(text.trim(), t.id); text = "" } },
                enabled = text.isNotBlank() && aiState !is AiState.Extracting,
                modifier = Modifier.size(Sp.iconButton),
                colors = IconButtonDefaults.iconButtonColors(containerColor = if (text.isNotBlank()) c.primary else c.statBg, contentColor = if (text.isNotBlank()) c.onPrimary else c.textSecondary),
            ) { Icon(Icons.AutoMirrored.Filled.Send, "Bhejo", Modifier.size(20.dp)) }
        }

        // End trip
        MsDestructiveButton("🔴 Trip Khatam Karo", onClick = { showEnd = true }, modifier = Modifier.padding(horizontal = Sp.screenHPad, vertical = 8.dp))
    }

    if (showEnd) {
        ModalBottomSheet(onDismissRequest = { showEnd = false }, containerColor = c.card) {
            Column(Modifier.padding(20.dp)) {
                Text("Pakka trip khatam karna hai?", style = MsType.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(16.dp))
                MsPrimaryButton("Haan, Khatam Karo", onClick = { viewModel.endTrip(t.id); onTripEnded(t.id) }, icon = "🔴")
                Spacer(Modifier.height(8.dp))
                MsSecondaryButton("Nahi, Chal Rahi Hai", onClick = { showEnd = false })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
