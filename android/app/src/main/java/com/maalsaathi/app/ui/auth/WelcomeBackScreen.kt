package com.maalsaathi.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*
import kotlinx.coroutines.delay

@Composable
fun WelcomeBackScreen(viewModel: AuthViewModel, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        viewModel.saveAuthForWhatsappUser()
        repeat(3) { delay(1000); countdown-- }
        onContinue()
    }

    val stats = state.existingUserStats ?: ExistingUserStats()

    Column(
        Modifier.fillMaxSize().background(MsCrimson).padding(horizontal = Sp.screenHPad),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Arre, wapas aaye!", style = MsType.headlineMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("${capWords(state.existingUserName)} bhai, sab yaad hai!", fontSize = 20.sp, color = Color.White.copy(0.9f))
        Spacer(Modifier.height(24.dp))

        Surface(shape = RoundedCornerShape(Sp.cardRadius), color = Color.White) {
            Column(Modifier.padding(Sp.cardPad), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aapka hisaab ready hai", fontSize = 13.sp, fontFamily = NotoSans, color = Ms.colors.textSecondary)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("Trips", "${stats.trips}", Modifier.weight(1f))
                    StatBox("Kamaai", formatRupees(stats.totalEarnings), Modifier.weight(1f), Ms.colors.primary)
                    StatBox("Is Mahine", formatRupees(stats.thisMonth), Modifier.weight(1f), Ms.colors.primary)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Inverted button: white bg, green text
        Surface(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().heightIn(min = Sp.primaryCta),
            shape = RoundedCornerShape(Sp.buttonRadius), color = Color.White,
        ) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Apna Data Dekho →", fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = MsCrimson)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("$countdown mein apne aap jayega...", fontSize = 13.sp, fontFamily = NotoSans, color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
    }
}
