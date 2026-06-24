package com.maalsaathi.app.ui.fleet

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.data.models.UserRole
import com.maalsaathi.app.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruckDetailScreen(
    viewModel: FleetViewModel,
    truckId: String,
    ongoingTrips: List<Trip>,
    allTrips: List<Trip>,
    onBack: () -> Unit,
    onInviteDriver: () -> Unit,
    onTripDetail: (String) -> Unit,
) {
    val trucks by viewModel.trucks.collectAsState()
    val truck = trucks.find { it.id == truckId }
    val c = Ms.colors
    val ctx = LocalContext.current
    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale("en", "IN")) }
    val dateFmt = remember { SimpleDateFormat("d MMM", Locale("hi", "IN")) }
    var selectedEntry by remember { mutableStateOf<TripEntry?>(null) }

    if (truck == null) {
        EmptyState("🚛", "Truck nahi mila", "Wapas jao", "← Wapas", onBack)
        return
    }

    val ongoingTrip = ongoingTrips.find { it.truckId == truckId }
    val truckTrips = allTrips.filter { it.truckId == truckId && it.status != TripStatus.ONGOING }.sortedByDescending { it.startTime }
    var imageRequired by remember { mutableStateOf(truck.imageRequired) }

    // Entry action bottom sheet (MAALIK only — edit/delete)
    if (selectedEntry != null) {
        ModalBottomSheet(onDismissRequest = { selectedEntry = null }, containerColor = c.card) {
            val entry = selectedEntry!!
            Column(Modifier.padding(20.dp)) {
                Text("${entry.emoji} ${capWords(entry.category)} — ${formatRupees(entry.amount)}", style = MsType.titleLarge)
                Text("by ${capWords(entry.driverName)} · ${timeFmt.format(Date(entry.timestamp))}", style = MsType.bodyMedium)
                Spacer(Modifier.height(16.dp))
                MsSecondaryButton("✏️ Edit Karo", icon = null, onClick = { selectedEntry = null /* TODO: edit flow */ })
                Spacer(Modifier.height(8.dp))
                MsDestructiveButton("🗑️ Delete Karo", icon = null, onClick = {
                    // TODO: viewModel.deleteEntry(entry.id)
                    selectedEntry = null
                })
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = Sp.screenHPad, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Text(truck.registrationNumber, style = MsType.headlineMedium, modifier = Modifier.weight(1f))
            if (ongoingTrip != null) StatusPill("🟢 LIVE", PillType.LIVE)
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(Sp.cardGap)) {
            // Truck info card
            item {
                MsCard {
                    if (truck.assignedDriverId != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Driver", style = MsType.labelLarge)
                                Text(capWords(truck.assignedDriverName), style = MsType.titleLarge)
                            }
                            IconButton(onClick = {
                                ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210")))
                            }) { Icon(Icons.Default.Phone, "Call", tint = c.primary) }
                        }
                    } else {
                        Text("Driver nahi hai", style = MsType.titleMedium.copy(color = c.warning))
                        Spacer(Modifier.height(8.dp))
                        MsPrimaryButton("👨‍✈️ Driver Add Karo", onClick = onInviteDriver, textSize = 14.sp)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Image zaroori?", style = MsType.titleMedium)
                            Text(if (imageRequired) "📷 Har kharche ka photo" else "📷 Optional hai", style = MsType.labelLarge)
                        }
                        Switch(checked = imageRequired, onCheckedChange = {
                            imageRequired = it
                            viewModel.setImageRequired(truckId, it)
                        }, colors = SwitchDefaults.colors(checkedTrackColor = c.primary, checkedThumbColor = c.card))
                    }

                    if (truck.model.isNotBlank()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = c.divider)
                        Text("Model: ${truck.model}", style = MsType.bodyMedium)
                    }
                }
            }

            // Ongoing trip with full journal
            if (ongoingTrip != null) {
                item { SectionHeader("Chal Rahi Trip") }
                item {
                    MsHighlightCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${capWords(ongoingTrip.origin)} → ${capWords(ongoingTrip.destination)}", style = MsType.headlineMedium, modifier = Modifier.weight(1f, false))
                            StatusPill("LIVE", PillType.LIVE)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${capWords(ongoingTrip.driverName)} · ${formatDuration(System.currentTimeMillis() - ongoingTrip.startTime)} se", style = MsType.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBox("Bhada", formatRupees(ongoingTrip.freightAmount), Modifier.weight(1f), c.profit)
                            StatBox("Kharcha", formatRupees(ongoingTrip.totalExpenses), Modifier.weight(1f), c.loss)
                            StatBox("Entries", "${ongoingTrip.entries.size}", Modifier.weight(1f))
                        }
                    }
                }

                // Full journal timeline
                if (ongoingTrip.entries.isNotEmpty()) {
                    item { SectionHeader("Journal") }

                    val sorted = ongoingTrip.entries.sortedBy { it.timestamp }
                    items(sorted, key = { it.id }) { entry ->
                        OwnerJournalRow(
                            entry = entry,
                            timeFmt = timeFmt,
                            isFirst = entry == sorted.first(),
                            isLast = entry == sorted.last(),
                            onEditDelete = { selectedEntry = it },
                        )
                    }
                }
            }

            // Past trips
            if (truckTrips.isNotEmpty()) {
                item { SectionHeader("Pichli Trips") }
                items(truckTrips.take(10), key = { it.id }) { trip ->
                    val net = trip.netProfit
                    Surface(onClick = { onTripDetail(trip.id) }, shape = RoundedCornerShape(Sp.cardRadius), color = c.card, border = BorderStroke(Sp.cardBorder, c.border)) {
                        Row(Modifier.fillMaxWidth().padding(Sp.cardPad), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleMedium)
                                Text("${dateFmt.format(Date(trip.startTime))} · ${capWords(trip.driverName)}", style = MsType.bodyMedium)
                            }
                            Text(formatRupees(net), style = MsType.amountMedium.copy(fontSize = 16.sp), color = if (net >= 0) c.profit else c.loss)
                        }
                    }
                }
            }

            if (truckTrips.isEmpty() && ongoingTrip == null) {
                item { EmptyState("🚛", "Is truck pe koi trip nahi", "Nayi trip shuru karo!") }
            }

            item { Spacer(Modifier.height(Sp.sectionGap)) }
        }
    }
}

@Composable
private fun OwnerJournalRow(
    entry: TripEntry,
    timeFmt: SimpleDateFormat,
    isFirst: Boolean,
    isLast: Boolean,
    onEditDelete: (TripEntry) -> Unit,
) {
    val c = Ms.colors
    val time = timeFmt.format(Date(entry.timestamp))
    val hasImage = entry.imageUrl != null

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Timeline dot + line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            if (!isFirst) Box(Modifier.width(1.5.dp).height(8.dp).background(c.border))
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (isFirst) c.primary else c.border))
            if (!isLast) Box(Modifier.width(1.5.dp).height(40.dp).background(c.border))
        }

        // Entry content
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 4.dp)) {
            // Driver name + time
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("[${capWords(entry.driverName)}] · $time", style = MsType.labelSmall)
                // MAALIK-only edit/delete
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(onClick = { onEditDelete(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = c.textSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onEditDelete(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = c.loss.copy(0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Category + amount
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.weight(1f, false)) {
                    Text("${entry.emoji} ", fontSize = 16.sp)
                    Text(capWords(entry.category), style = MsType.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (entry.note.isNotBlank()) Text(" — ${entry.note}", style = MsType.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (entry.amount > 0) {
                    val color = if (entry.type == EntryType.EXPENSE) c.loss else c.primary
                    val prefix = if (entry.type == EntryType.EXPENSE) "−" else "+"
                    Text("$prefix${formatRupees(entry.amount)}", style = MsType.amountMedium.copy(fontSize = 15.sp), color = color)
                }
            }

            // Image thumbnail (if present)
            if (hasImage) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp), color = c.statBg,
                    border = BorderStroke(Sp.cardBorder, c.border),
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, "Photo", tint = c.textSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            } else if (entry.imageRequired) {
                Spacer(Modifier.height(4.dp))
                Text("⚠️ Photo nahi hai", style = MsType.labelSmall.copy(color = c.warning))
            }

            // "Driver ki entry" label
            Text("Driver ki entry", style = MsType.labelSmall.copy(color = c.textSecondary.copy(0.6f)))
        }
    }
}
