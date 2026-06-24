package com.maalsaathi.app.ui.driver

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.ui.common.*
import com.maalsaathi.app.ui.trip.TripViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverHomeScreen(
    tripVm: TripViewModel,
    driverName: String,
    truckNumber: String,
    orgName: String,
    ownerPhone: String,
    onOngoingTrip: () -> Unit,
    onEndTrip: (String) -> Unit,
    onNewTrip: () -> Unit,
    onTripDetail: (String) -> Unit,
) {
    val ongoing by tripVm.ongoingTrip.collectAsState()
    val past by tripVm.pastTrips.collectAsState()
    val c = Ms.colors
    val ctx = LocalContext.current
    val dateFmt = SimpleDateFormat("d MMM", Locale("hi", "IN"))

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Namaste, ${capWords(driverName)} ji!", style = MsType.headlineLarge)
            Text("$truckNumber · ${capWords(orgName)}", style = MsType.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        // Ongoing trip — CRED card
        if (ongoing != null) {
            val trip = ongoing!!
            item {
                Surface(
                    onClick = onOngoingTrip,
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = c.primary.copy(0.12f)),
                    shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                ) {
                    Row {
                        Box(Modifier.width(3.dp).height(140.dp).background(c.primary, RoundedCornerShape(topStart = Sp.cardRadius, bottomStart = Sp.cardRadius)))
                        Column(Modifier.weight(1f).padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.headlineMedium, modifier = Modifier.weight(1f, false))
                                StatusPill("Live", PillType.LIVE)
                            }
                            Spacer(Modifier.height(4.dp))
                            val sub = buildString {
                                if (trip.cargoType.isNotBlank()) append("${capWords(trip.cargoType)} · ")
                                if (trip.cargoWeightTons > 0) append("${formatWeight(trip.cargoWeightTons)} · ")
                                append(formatDuration(System.currentTimeMillis() - trip.startTime))
                            }
                            Text(sub, style = MsType.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)

                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                                StatBox("Bhada", formatRupees(trip.freightAmount), Modifier.weight(1f), c.profit)
                                StatBox("Kharcha", formatRupees(trip.totalExpenses), Modifier.weight(1f), c.loss)
                            }

                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Kharcha Daalo →", style = MsType.titleMedium.copy(color = c.primary))
                                Text("Khatam Karo →", style = MsType.titleMedium.copy(color = c.loss))
                            }
                        }
                    }
                }
            }
        }

        // New trip
        item {
            Spacer(Modifier.height(4.dp))
            MsPrimaryButton("+ Nayi Trip Shuru Karo", onClick = onNewTrip)
        }

        // Past trips — flat rows, read only
        if (past.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Pichli trips", style = MsType.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(past.take(10), key = { it.id }) { trip ->
                val net = trip.netProfit
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${capWords(trip.origin)} → ${capWords(trip.destination)}", style = MsType.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${dateFmt.format(Date(trip.startTime))}${if (trip.cargoType.isNotBlank()) " · ${capWords(trip.cargoType)}" else ""}", style = MsType.bodyMedium)
                    }
                    Text(formatRupees(net), style = MsType.amountMedium.copy(fontSize = 16.sp), color = if (net >= 0) c.profit else c.loss)
                }
                ThinDivider()
            }
        }

        if (ongoing == null && past.isEmpty()) {
            item { EmptyState("🚛", "Koi trip nahi abhi", "Pehli trip shuru karo!", "Nayi Trip Shuru Karo", onNewTrip) }
        }

        // Contact owner
        item {
            Spacer(Modifier.height(8.dp))
            MsSecondaryButton("Owner se baat karo", onClick = {
                if (ownerPhone.isNotBlank()) ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$ownerPhone")))
            })
            Spacer(Modifier.height(20.dp))
        }
    }
}
