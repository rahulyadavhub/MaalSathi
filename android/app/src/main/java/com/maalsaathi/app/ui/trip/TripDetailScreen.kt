package com.maalsaathi.app.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.ui.common.*

@Composable
fun TripDetailScreen(viewModel: TripViewModel, tripId: String, onBack: () -> Unit, onRepeat: (Trip) -> Unit) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    LaunchedEffect(tripId) { trip = viewModel.getTripById(tripId) }
    val t = trip ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Trip nahi mili", style = MsType.bodyLarge, color = Ms.colors.textSecondary)
                Spacer(Modifier.height(12.dp))
                MsSecondaryButton("Wapas Jao", onClick = onBack)
            }
        }; return
    }
    val c = Ms.colors
    val dateFmt = remember { java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("hi", "IN")) }
    val timeFmt = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale("en", "IN")) }
    val entryTimeFmt = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale("en", "IN")) }

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = Sp.screenHPad), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Text("Trip Details", style = MsType.headlineMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            val (label, type) = if (t.status == TripStatus.COMPLETED) "✅ Poori" to PillType.DONE else "❌ Cancel" to PillType.CANCELLED
            StatusPill(label, type)
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Sp.screenHPad)) {
            Spacer(Modifier.height(8.dp))
            Text("${capWords(t.origin)} → ${capWords(t.destination)}", style = MsType.headlineMedium, color = c.textPrimary)
            Text(dateFmt.format(java.util.Date(t.startTime)), style = MsType.bodyMedium)
            t.durationMillis?.let {
                Text("${timeFmt.format(java.util.Date(t.startTime))} — ${timeFmt.format(java.util.Date(t.endTime!!))} · ${formatDuration(it)}", style = MsType.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            // P&L
            MsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💰 Bhada", fontSize = 17.sp); Text(formatRupees(t.freightAmount), fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.profit)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider, thickness = 0.5.dp)
                t.entries.filter { it.type == EntryType.EXPENSE }.forEach { e ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${e.emoji} ${capWords(e.category)}", fontSize = 16.sp)
                        Text("−${formatRupees(e.amount)}", fontSize = 16.sp, color = c.loss)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Kharcha", fontWeight = FontWeight.Bold); Text(formatRupees(t.totalExpenses), fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.loss)
                }
            }

            Spacer(Modifier.height(12.dp))
            val net = t.netProfit; val pos = net >= 0
            Surface(shape = RoundedCornerShape(Sp.cardRadius), color = if (pos) c.primary.copy(0.08f) else c.loss.copy(0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (pos) c.profit else c.loss)) {
                Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (pos) "↑ Net Fayda" else "↓ Nuksan", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = if (pos) c.profit else c.loss)
                        Text(formatRupees(kotlin.math.abs(net)), style = MsType.headlineMedium, color = if (pos) c.profit else c.loss)
                    }
                }
            }

            // Timeline
            if (t.entries.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionHeader("Timeline")
                Spacer(Modifier.height(4.dp))
                t.entries.sortedBy { it.timestamp }.forEachIndexed { i, e ->
                    JournalEntryRow(entryTimeFmt.format(java.util.Date(e.timestamp)), e.emoji,
                        "${capWords(e.category)}${if (e.note.isNotBlank()) " — ${e.note}" else ""}",
                        e.amount, e.type, isFirst = i == 0, isLast = i == t.entries.lastIndex)
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        MsSecondaryButton("🔄 Dobara Chalao", onClick = { onRepeat(t) }, modifier = Modifier.padding(horizontal = Sp.screenHPad, vertical = 12.dp))
    }
}
