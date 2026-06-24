package com.maalsaathi.app.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripScreen(
    viewModel: TripViewModel,
    onNewTrip: () -> Unit,
    onOngoingTrip: () -> Unit,
    onEndTrip: (String) -> Unit,
    onTripDetail: (String) -> Unit,
    onStartScheduled: (String) -> Unit,
) {
    val ongoing by viewModel.ongoingTrip.collectAsState()
    val scheduled by viewModel.scheduledTrips.collectAsState()
    val past by viewModel.pastTrips.collectAsState()
    val c = Ms.colors
    val hasAny = ongoing != null || scheduled.isNotEmpty() || past.isNotEmpty()

    if (!hasAny) {
        Box(Modifier.fillMaxSize()) {
            EmptyState("🚛", "Koi trip nahi abhi", "Pehli trip shuru karo!", "Nayi Trip Shuru Karo", onNewTrip)
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(Sp.cardGap)) {
        item {
            Spacer(Modifier.height(Sp.sectionGap))
            Text("Namaste, Rahul ji! 👋", style = MsType.headlineMedium, color = c.textPrimary)
            Text("MH12AB1234", style = MsType.bodyMedium)
            Spacer(Modifier.height(4.dp))
        }

        // A — Ongoing
        if (ongoing != null) {
            item { SectionHeader("Chal Rahi Hai 🟢") }
            item { OngoingCard(ongoing!!, onGo = onOngoingTrip, onEnd = { onEndTrip(ongoing!!.id) }) }
        }

        // B — New trip
        item { Spacer(Modifier.height(4.dp)); MsPrimaryButton("🚛 Nayi Trip Shuru Karo", onClick = onNewTrip) }

        // C — Scheduled
        if (scheduled.isNotEmpty()) {
            item { SectionHeader("Aane Wali Trips 📅") }
            items(scheduled, key = { it.id }) { trip ->
                ScheduledCard(trip, onStart = { onStartScheduled(trip.id) }, onCancel = { viewModel.cancelTrip(trip.id) })
            }
        }

        // D — Past
        if (past.isNotEmpty()) {
            item { SectionHeader("Pichli Trips") }
            items(past, key = { it.id }) { trip -> PastCard(trip, onClick = { onTripDetail(trip.id) }) }
        }

        item { Spacer(Modifier.height(Sp.sectionGap)) }
    }
}

@Composable
private fun OngoingCard(trip: Trip, onGo: () -> Unit, onEnd: () -> Unit) {
    val elapsed = System.currentTimeMillis() - trip.startTime
    val c = Ms.colors
    MsHighlightCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.headlineMedium, color = c.textPrimary, modifier = Modifier.weight(1f, false))
            StatusPill("LIVE", PillType.LIVE)
        }
        Spacer(Modifier.height(4.dp))
        val sub = buildString {
            if (trip.cargoType.isNotBlank()) append("${capWords(trip.cargoType)} · ")
            if (trip.cargoWeightTons > 0) append("${formatWeight(trip.cargoWeightTons)} · ")
            append("${formatDuration(elapsed)} se chal rahi")
        }
        Text(sub, style = MsType.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Bhada", formatRupees(trip.freightAmount), Modifier.weight(1f), c.profit)
            StatBox("Kharcha", formatRupees(trip.totalExpenses), Modifier.weight(1f), c.loss)
            StatBox("Entries", "${trip.entries.size}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MsPrimaryButton("🎙️ Kharcha Daalo", onClick = onGo, modifier = Modifier.weight(1f), textSize = 13.sp)
            MsDestructiveButton("🔴 Khatam Karo", onClick = onEnd, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScheduledCard(trip: Trip, onStart: () -> Unit, onCancel: () -> Unit) {
    val dateFmt = SimpleDateFormat("d MMM", Locale("hi", "IN"))
    val dateStr = trip.scheduledDate?.let { dateFmt.format(Date(it)) } ?: ""
    MsWarningCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleLarge, modifier = Modifier.weight(1f, false))
            StatusPill("📅 $dateStr", PillType.SCHEDULED)
        }
        if (trip.partyName.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(capWords(trip.partyName), style = MsType.bodyMedium)
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MsPrimaryButton("Abhi Shuru Karo", onClick = onStart, modifier = Modifier.weight(1f))
            MsTextButton("Cancel", onClick = onCancel)
        }
    }
}

@Composable
private fun PastCard(trip: Trip, onClick: () -> Unit) {
    val dateFmt = SimpleDateFormat("d MMM", Locale("hi", "IN"))
    val c = Ms.colors
    val (label, type) = if (trip.status == TripStatus.COMPLETED) "✅ Poori" to PillType.DONE else "❌ Cancel" to PillType.CANCELLED
    MsCard(borderColor = c.border) {
        androidx.compose.material3.Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent) {
            androidx.compose.foundation.layout.Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleLarge, modifier = Modifier.weight(1f, false))
                    StatusPill(label, type)
                }
                Spacer(Modifier.height(2.dp))
                val sub = buildString {
                    append(dateFmt.format(Date(trip.startTime)))
                    if (trip.cargoType.isNotBlank()) append(" · ${capWords(trip.cargoType)}")
                    trip.durationMillis?.let { append(" · ${formatDuration(it)}") }
                }
                Text(sub, style = MsType.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("Bhada", formatRupees(trip.freightAmount), Modifier.weight(1f), c.profit)
                    StatBox("Kharcha", formatRupees(trip.totalExpenses), Modifier.weight(1f), c.loss)
                    val net = trip.netProfit
                    StatBox("Fayda", formatRupees(net), Modifier.weight(1f), if (net >= 0) c.profit else c.loss)
                }
            }
        }
    }
}
