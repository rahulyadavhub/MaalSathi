package com.maalsaathi.app.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*

@Composable
fun TripConfirmScreen(viewModel: TripViewModel, onBack: () -> Unit, onConfirmed: (String) -> Unit, onEdit: () -> Unit) {
    val aiState by viewModel.aiState.collectAsState()
    val ext = (aiState as? AiState.TripExtracted)?.data ?: return
    val c = Ms.colors

    Column(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad)) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Text("Trip Check Karo", style = MsType.headlineMedium, color = c.textPrimary)
        }
        Spacer(Modifier.height(12.dp))

        if (ext.confidence == "low") {
            MsWarningCard { Text("⚠️ AI ko pakka nahi tha — details check karo", style = MsType.bodyLarge, color = c.warning) }
            Spacer(Modifier.height(12.dp))
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            MsCard {
                Text("Trip Details", style = MsType.titleMedium, color = c.textSecondary)
                Spacer(Modifier.height(12.dp))
                DetailRow("📍", "Kahan se", capWords(ext.origin))
                ThinDivider()
                DetailRow("📍", "Kahan tak", capWords(ext.destination))
                if (ext.cargoType.isNotBlank()) {
                    ThinDivider()
                    val cargo = buildString {
                        append(capWords(ext.cargoType))
                        if (ext.cargoWeightTons > 0) append(" — ${formatWeight(ext.cargoWeightTons)}")
                    }
                    DetailRow("📦", "Maal", cargo)
                }
                if (ext.freightAmount > 0) {
                    ThinDivider()
                    DetailRow("💰", "Bhada", formatRupees(ext.freightAmount), valueColor = c.profit, valueLarge = true)
                }
                if (ext.advanceAmount > 0) {
                    ThinDivider()
                    DetailRow("💵", "Advance", formatRupees(ext.advanceAmount))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        MsPrimaryButton("✅ Haan, Sahi Hai — Trip Shuru Karo", onClick = {
            val id = viewModel.createTrip(ext)
            viewModel.resetAiState()
            onConfirmed(id)
        })
        Spacer(Modifier.height(8.dp))
        MsSecondaryButton("✏️ Badlo", onClick = { viewModel.resetAiState(); onEdit() })
        Spacer(Modifier.height(4.dp))
        MsTextButton("Haath se bharo", onClick = onEdit, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(emoji: String, label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = Ms.colors.textPrimary, valueLarge: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(emoji, fontSize = 18.sp)
        Column {
            Text(label, style = MsType.labelLarge)
            Text(value, fontSize = if (valueLarge) 20.sp else 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = valueColor)
        }
    }
}

@Composable
private fun ThinDivider() = HorizontalDivider(thickness = 0.5.dp, color = Ms.colors.divider)
