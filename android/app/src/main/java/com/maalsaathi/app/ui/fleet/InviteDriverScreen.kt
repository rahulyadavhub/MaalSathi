package com.maalsaathi.app.ui.fleet

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.maalsaathi.app.data.models.DriverInvite
import com.maalsaathi.app.data.models.Truck
import com.maalsaathi.app.ui.common.*

@Composable
fun InviteDriverScreen(viewModel: FleetViewModel, truckId: String, onBack: () -> Unit) {
    val trucks by viewModel.trucks.collectAsState()
    val state by viewModel.state.collectAsState()
    val truck = trucks.find { it.id == truckId }
    val c = Ms.colors
    val ctx = LocalContext.current
    var sentInvite by remember { mutableStateOf<DriverInvite?>(null) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = Sp.screenHPad, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary) }
            Text("Driver Add Karo", style = MsType.headlineMedium)
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Sp.screenHPad)) {
            Spacer(Modifier.height(16.dp))

            if (truck != null) {
                MsCard {
                    Text("Truck", style = MsType.labelLarge)
                    Text(truck.registrationNumber, style = MsType.headlineMedium)
                    if (truck.imageRequired) {
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = c.primary.copy(0.1f)) {
                            Text("📷 Is truck pe image zaroori hai", style = MsType.labelLarge.copy(color = c.primary), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (sentInvite == null) {
                Text("Invite link banao aur WhatsApp pe bhejo", style = MsType.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))

                MsPrimaryButton("📤 Invite Link Banao", onClick = {
                    if (truck != null) {
                        val invite = viewModel.generateInvite(truck.id, truck.registrationNumber, state.ownerName)
                        sentInvite = invite
                        shareInvite(ctx, invite)
                    }
                })
            } else {
                // Invite sent
                MsHighlightCard {
                    Text("✅ Link bhej diya!", style = MsType.titleLarge.copy(color = c.primary))
                    Spacer(Modifier.height(8.dp))
                    Text("⏳ Driver ke join karne ka wait kar rahe hain", style = MsType.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = c.statBg) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Invite Code", style = MsType.labelLarge)
                            Text(sentInvite!!.inviteCode, style = MsType.headlineLarge.copy(letterSpacing = 4.sp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val daysLeft = ((sentInvite!!.expiresAt - System.currentTimeMillis()) / 86_400_000).toInt()
                    Text("$daysLeft din mein expire hoga", style = MsType.labelSmall)
                }

                Spacer(Modifier.height(16.dp))
                MsSecondaryButton("📤 Dobara WhatsApp Pe Bhejo", onClick = { shareInvite(ctx, sentInvite!!) })
            }
        }
    }
}

private fun shareInvite(ctx: Context, invite: DriverInvite) {
    val text = buildString {
        appendLine("🚛 ${capWords(invite.ownerName)} ne aapko MaalSaathi pe add kiya hai!")
        appendLine()
        appendLine("${capWords(invite.ownerName)} ke saath kaam shuru karo.")
        appendLine("Truck: ${invite.truckNumber}")
        appendLine()
        appendLine("Yahan click karo aur join karo:")
        appendLine(invite.inviteLink)
        appendLine()
        appendLine("Code: ${invite.inviteCode}")
        appendLine()
        append("— MaalSaathi")
    }
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Driver ko invite bhejo"))
}
