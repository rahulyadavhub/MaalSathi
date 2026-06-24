package com.maalsaathi.app.ui.trip

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.ui.common.*

@Composable
fun TripSummaryScreen(viewModel: TripViewModel, tripId: String, onGoHome: () -> Unit) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    LaunchedEffect(tripId) { trip = viewModel.getTripById(tripId) }
    val t = trip ?: run { Box(Modifier.fillMaxSize(), Alignment.Center) { SkeletonCard(height = 200.dp) }; return }
    val ctx = LocalContext.current; val c = Ms.colors
    val timeFmt = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale("en", "IN")) }
    val dateFmt = remember { java.text.SimpleDateFormat("d MMM", java.util.Locale("hi", "IN")) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Sp.screenHPad)) {
        Spacer(Modifier.height(Sp.sectionGap))
        Text("Trip Khatam! ✅", style = MsType.headlineLarge, color = c.primary)
        Spacer(Modifier.height(4.dp))
        Text("${capWords(t.origin)} → ${capWords(t.destination)}", fontSize = 18.sp, color = c.textSecondary)
        t.durationMillis?.let {
            Text("🕐 Shuru: ${timeFmt.format(java.util.Date(t.startTime))} · Khatam: ${timeFmt.format(java.util.Date(t.endTime!!))} · ${formatDuration(it)}", fontSize = 14.sp, color = c.textSecondary)
        }

        Spacer(Modifier.height(Sp.sectionGap))

        // P&L Card
        MsCard {
            Text("Hisaab", style = MsType.titleMedium, color = c.textSecondary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("💰 Bhada", fontSize = 17.sp, color = c.textPrimary)
                Text(formatRupees(t.freightAmount), fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.profit)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider, thickness = 0.5.dp)

            val expenses = t.entries.filter { it.type == EntryType.EXPENSE }
            expenses.forEach { e ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${e.emoji} ${capWords(e.category)}", fontSize = 16.sp, color = c.textPrimary)
                    Text("−${formatRupees(e.amount)}", fontSize = 16.sp, color = c.loss)
                }
            }
            if (expenses.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Kharcha", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary)
                    Text(formatRupees(t.totalExpenses), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.loss)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Net fayda box
        val net = t.netProfit; val profit = net >= 0
        Surface(shape = RoundedCornerShape(Sp.cardRadius),
            color = if (profit) c.profit.copy(0.08f) else c.loss.copy(0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (profit) c.profit else c.loss)) {
            Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (profit) "↑ Net Fayda" else "↓ Nuksan", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = if (profit) c.profit else c.loss)
                    Text(formatRupees(kotlin.math.abs(net)), style = MsType.headlineMedium, color = if (profit) c.profit else c.loss)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        MsPrimaryButton("📤 WhatsApp Pe Share Karo", onClick = { shareSummary(ctx, t) })
        Spacer(Modifier.height(8.dp))
        MsTextButton("Trip List Pe Jao", onClick = onGoHome, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Sp.sectionGap))
    }
}

private fun shareSummary(ctx: Context, t: Trip) {
    val dateFmt = java.text.SimpleDateFormat("d MMM", java.util.Locale("hi", "IN"))
    val text = buildString {
        appendLine("🚛 MaalSaathi — Trip Report")
        appendLine("━━━━━━━━━━━━━━━")
        appendLine("📍 ${capWords(t.origin)} → ${capWords(t.destination)}")
        append("📅 ${dateFmt.format(java.util.Date(t.startTime))}")
        t.durationMillis?.let { append(" · ⏱ ${formatDuration(it)}") }
        appendLine()
        appendLine("━━━━━━━━━━━━━━━")
        appendLine("💰 Bhada:    ${formatRupees(t.freightAmount)}")
        t.entries.filter { it.type == EntryType.EXPENSE }.groupBy { it.category }.forEach { (cat, es) ->
            appendLine("${es.first().emoji} ${capWords(cat)}:   ${formatRupees(es.sumOf { it.amount })}")
        }
        appendLine("━━━━━━━━━━━━━━━")
        val net = t.netProfit
        appendLine(if (net >= 0) "✅ Net Fayda: ${formatRupees(net)}" else "❌ Nuksan: ${formatRupees(kotlin.math.abs(net))}")
        appendLine("━━━━━━━━━━━━━━━")
        append("MaalSaathi — Apna Digital Munshi")
    }
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share Karo"))
}
