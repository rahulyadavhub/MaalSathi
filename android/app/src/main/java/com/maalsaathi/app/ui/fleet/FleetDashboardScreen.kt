package com.maalsaathi.app.ui.fleet

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.data.models.Truck
import com.maalsaathi.app.ui.common.*

@Composable
fun FleetDashboardScreen(
    viewModel: FleetViewModel,
    ongoingTrips: List<Trip>,
    onTruckDetail: (String) -> Unit,
    onAddTruck: () -> Unit,
    onInviteDriver: (String) -> Unit,
    onStartTrip: (String) -> Unit,
    onProfileClick: () -> Unit = {},
) {
    val trucks by viewModel.trucks.collectAsState()
    val pending by viewModel.pendingInvites.collectAsState()
    val state by viewModel.state.collectAsState()
    val c = Ms.colors

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Namaste, ${capWords(state.ownerName)} ji!", style = MsType.headlineLarge)
                    Text(capWords(state.orgName), style = MsType.bodyMedium)
                }
                ProfileAvatar(state.ownerName, onClick = onProfileClick)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Stats — shadow cards, no borders
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox("Trucks", "${trucks.size}", Modifier.weight(1f))
                StatBox("Aaj Kamaai", formatRupees(state.todayEarnings), Modifier.weight(1f), c.profit)
                StatBox("Is Mahine", formatRupees(state.monthEarnings), Modifier.weight(1f), c.profit)
            }
        }

        // Trucks
        item {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Trucks", style = MsType.titleMedium)
                TextButton(onClick = onAddTruck) {
                    Text("+ Add Karo", style = MsType.labelLarge.copy(color = c.primary, fontWeight = FontWeight.Bold))
                }
            }
        }

        items(trucks, key = { it.id }) { truck ->
            val trip = ongoingTrips.find { it.truckId == truck.id }
            CleanTruckCard(truck, trip, onTap = { onTruckDetail(truck.id) }, onInvite = { onInviteDriver(truck.id) }, onStart = { onStartTrip(truck.id) })
        }

        // Recent activity — flat rows, no cards
        if (state.recentActivity.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Abhi kya ho raha hai", style = MsType.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(state.recentActivity.take(6), key = { it.id }) { entry ->
                ActivityRow(entry)
                ThinDivider()
            }
        }

        // Pending invites
        if (pending.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Pending invites", style = MsType.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(pending, key = { it.id }) { invite ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(invite.truckNumber, style = MsType.titleMedium)
                        Text("Driver ka wait... · ${formatTimeAgo(invite.createdAt)}", style = MsType.labelSmall)
                    }
                    StatusPill("Pending", PillType.SCHEDULED)
                }
                ThinDivider()
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun CleanTruckCard(truck: Truck, trip: Trip?, onTap: () -> Unit, onInvite: () -> Unit, onStart: () -> Unit) {
    val c = Ms.colors
    val hasDriver = truck.assignedDriverId != null
    val isLive = trip != null

    Surface(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = Color.Black.copy(0.08f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
    ) {
        Row {
            // Green accent bar for live trucks
            if (isLive) Box(Modifier.width(3.dp).height(120.dp).background(c.primary, RoundedCornerShape(topStart = Sp.cardRadius, bottomStart = Sp.cardRadius)))

            Column(Modifier.weight(1f).padding(16.dp)) {
                // Row 1: Truck number + status
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(truck.registrationNumber, style = MsType.titleLarge)
                    when {
                        isLive -> StatusPill("Live", PillType.LIVE)
                        !hasDriver -> StatusPill("Driver nahi", PillType.SCHEDULED)
                        else -> StatusPill("Khali", PillType.DONE)
                    }
                }

                // Row 2: Driver
                if (hasDriver) {
                    Spacer(Modifier.height(2.dp))
                    Text(capWords(truck.assignedDriverName), style = MsType.bodyMedium)
                }

                // Row 3: Route + stats (live only)
                if (isLive && trip != null) {
                    Spacer(Modifier.height(6.dp))
                    Text("${capWords(trip.origin)} → ${capWords(trip.destination)} · ${formatDuration(System.currentTimeMillis() - trip.startTime)}", style = MsType.bodyMedium.copy(color = c.textPrimary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("${formatRupees(trip.totalExpenses)} kharcha", style = MsType.labelLarge.copy(color = c.loss, fontWeight = FontWeight.Bold))
                }

                // Action text link
                Spacer(Modifier.height(8.dp))
                when {
                    !hasDriver -> Text("Driver Add Karo →", style = MsType.titleMedium.copy(color = c.warning), modifier = Modifier.padding(vertical = 4.dp))
                    isLive -> Text("Dekho →", style = MsType.titleMedium.copy(color = c.primary), modifier = Modifier.padding(vertical = 4.dp))
                    else -> Text("Trip Shuru Karo →", style = MsType.titleMedium.copy(color = c.primary), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(entry: TripEntry) {
    val c = Ms.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        // Emoji in circle
        Box(Modifier.size(36.dp).clip(CircleShape).background(c.statBg), contentAlignment = Alignment.Center) {
            Text(entry.emoji, fontSize = 16.sp)
        }
        // Info
        Column(Modifier.weight(1f)) {
            Text(capWords(entry.driverName), style = MsType.titleMedium.copy(fontSize = 14.sp))
            Text(capWords(entry.category), style = MsType.bodyMedium.copy(fontSize = 13.sp))
        }
        // Amount + time
        Column(horizontalAlignment = Alignment.End) {
            if (entry.amount > 0) Text(formatRupees(entry.amount), style = MsType.amountMedium.copy(fontSize = 15.sp), color = c.loss)
            Text(formatTimeAgo(entry.timestamp), style = MsType.labelSmall)
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60_000
    return when {
        mins < 1 -> "abhi"
        mins < 60 -> "$mins min"
        mins < 1440 -> "${mins / 60}h"
        else -> "${mins / 1440}d"
    }
}
