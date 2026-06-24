package com.maalsaathi.app.ui.hisaab

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maalsaathi.app.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HisaabScreen(viewModel: HisaabViewModel, onTripDetail: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(Sp.cardGap)) {
        item {
            Spacer(Modifier.height(Sp.sectionGap))
            Text("Hisaab", style = MsType.headlineLarge, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
        }

        // Period toggle
        item {
            Surface(shape = RoundedCornerShape(Sp.buttonRadius), color = c.card, border = BorderStroke(1.dp, c.border)) {
                Row(Modifier.padding(4.dp)) {
                    HisaabPeriod.entries.forEach { p ->
                        val sel = state.period == p
                        Surface(onClick = { viewModel.setPeriod(p) }, modifier = Modifier.weight(1f).height(Sp.iconButton),
                            shape = RoundedCornerShape(10.dp),
                            color = if (sel) c.primary else c.card) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text(p.label, style = MsType.labelLarge,
                                    color = if (sel) c.onPrimary else c.textSecondary, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }

        // Summary
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox("Kamaai", formatRupees(state.totalBhada), Modifier.weight(1f), c.profit)
                StatBox("Kharcha", formatRupees(state.totalKharcha), Modifier.weight(1f), c.loss)
                StatBox("Fayda", formatRupees(state.netFayda), Modifier.weight(1f), if (state.netFayda >= 0) c.profit else c.loss)
            }
        }

        // Trip-wise
        if (state.trips.isNotEmpty()) {
            item { SectionHeader("Trip Wise") }
            items(state.trips, key = { it.id }) { trip ->
                val dateFmt = SimpleDateFormat("d MMM", Locale("hi", "IN"))
                Surface(onClick = { onTripDetail(trip.id) }, shape = RoundedCornerShape(Sp.cardRadius), color = c.card, border = BorderStroke(1.dp, c.border)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(Sp.cardPad), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleMedium, color = c.textPrimary)
                            Text(dateFmt.format(Date(trip.startTime)), style = MsType.bodyMedium)
                        }
                        val net = trip.netProfit
                        Text(formatRupees(net), style = MsType.titleMedium, fontWeight = FontWeight.Bold, color = if (net >= 0) c.profit else c.loss)
                    }
                }
            }
        }

        // Category breakdown
        if (state.categoryBreakdown.isNotEmpty()) {
            item { SectionHeader("Kharcha Kahan Gaya") }
            items(state.categoryBreakdown) { cat ->
                MsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.emoji)
                            Text(cat.category.replaceFirstChar { it.uppercase() }, style = MsType.bodyLarge, color = c.textPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupees(cat.total), style = MsType.bodyLarge, fontWeight = FontWeight.Bold, color = c.loss)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.fillMaxWidth(0.35f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.statBg)) {
                                Box(Modifier.fillMaxWidth(cat.percent).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.primary))
                            }
                        }
                    }
                }
            }
        }

        // Empty
        if (state.trips.isEmpty()) {
            item { EmptyState("💰", "Koi hisaab nahi", "Trip karo toh hisaab dikhega") }
        }

        item { Spacer(Modifier.height(Sp.sectionGap)) }
    }
}
