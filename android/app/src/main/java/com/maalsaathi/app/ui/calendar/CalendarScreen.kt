package com.maalsaathi.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsState()
    val upcoming by viewModel.upcomingReminders.collectAsState()
    val c = Ms.colors
    val cal = Calendar.getInstance().apply { set(Calendar.YEAR, state.selectedYear); set(Calendar.MONTH, state.selectedMonth); set(Calendar.DAY_OF_MONTH, 1) }
    val monthFmt = SimpleDateFormat("MMMM yyyy", Locale("hi", "IN"))
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val today = Calendar.getInstance()
    val isThisMonth = state.selectedYear == today.get(Calendar.YEAR) && state.selectedMonth == today.get(Calendar.MONTH)
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(Sp.cardGap)) {
        item {
            Spacer(Modifier.height(Sp.sectionGap))
            Text("Calendar", style = MsType.headlineLarge, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
        }

        // Month nav
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.changeMonth(-1) }, Modifier.size(Sp.iconButton)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = c.textPrimary) }
                Text(monthFmt.format(cal.time), fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary)
                IconButton(onClick = { viewModel.changeMonth(1) }, Modifier.size(Sp.iconButton)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", tint = c.textPrimary) }
            }
        }

        // Day headers
        item {
            Row(Modifier.fillMaxWidth()) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, fontFamily = NotoSans, color = c.textSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Grid
        item {
            val rows = (firstDow + daysInMonth + 6) / 7
            Column {
                repeat(rows) { row ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val day = row * 7 + col - firstDow + 1
                            Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), Alignment.Center) {
                                if (day in 1..daysInMonth) {
                                    val sel = state.selectedDay == day
                                    val isToday = isThisMonth && day == todayDay

                                    Box(
                                        Modifier.size(40.dp).clip(CircleShape)
                                            .background(if (sel) c.primary else if (isToday) c.textPrimary else Color.Transparent)
                                            .clickable { viewModel.selectDay(day) },
                                        Alignment.Center,
                                    ) {
                                        Text("$day", fontSize = 15.sp, color = if (sel || isToday) c.onPrimary else c.textPrimary, fontWeight = if (sel || isToday) FontWeight.Bold else FontWeight.Normal)
                                    }

                                    // Dots
                                    val dayCal = Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, day, 0, 0, 0) }
                                    val ds = dayCal.timeInMillis; dayCal.add(Calendar.DAY_OF_MONTH, 1); val de = dayCal.timeInMillis
                                    val hasTrip = state.scheduledTrips.any { (it.scheduledDate ?: it.startTime) in ds until de }
                                    val hasRem = state.reminders.any { it.dueDate in ds until de && !it.isDone }
                                    if (hasTrip || hasRem) {
                                        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (hasTrip) Box(Modifier.size(4.dp).clip(CircleShape).background(c.primary))
                                            if (hasRem) Box(Modifier.size(4.dp).clip(CircleShape).background(c.warning))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected day
        if (state.selectedDay != null && (state.selectedDateTrips.isNotEmpty() || state.selectedDateReminders.isNotEmpty())) {
            item { SectionHeader("${state.selectedDay} ${SimpleDateFormat("MMMM", Locale("hi", "IN")).format(cal.time)}") }
            items(state.selectedDateTrips) { trip ->
                MsCard { Text("📅 ${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleMedium, color = c.textPrimary) }
            }
            items(state.selectedDateReminders) { rem ->
                MsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(rem.title, style = MsType.titleMedium, color = c.textPrimary)
                        rem.amount?.let { Text(formatRupees(it), style = MsType.titleMedium, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.warning) }
                    }
                }
            }
        }

        // Upcoming reminders
        if (upcoming.isNotEmpty()) {
            item { SectionHeader("Aane Wale Reminders") }
            items(upcoming, key = { it.id }) { rem ->
                val dateFmt = SimpleDateFormat("d MMM", Locale("hi", "IN"))
                val overdue = rem.dueDate < System.currentTimeMillis()
                Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (overdue) c.loss else c.border)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(Sp.cardPad), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(rem.title, style = MsType.titleMedium, color = if (overdue) c.loss else c.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(dateFmt.format(Date(rem.dueDate)), style = MsType.bodyMedium)
                                rem.partyName?.let { Text(capWords(it), style = MsType.bodyMedium) }
                            }
                        }
                        rem.amount?.let { Text(formatRupees(it), style = MsType.titleMedium, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.warning) }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
