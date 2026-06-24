package com.maalsaathi.app.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.MsCrimson
import com.maalsaathi.app.ui.common.MsType
import com.maalsaathi.app.ui.common.Poppins
import com.maalsaathi.app.ui.common.capWords
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(viewModel: AuthViewModel, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var animateCheck by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animateCheck) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "check",
    )

    LaunchedEffect(Unit) {
        delay(200)
        animateCheck = true
        delay(2500)
        onContinue()
    }

    Column(
        Modifier.fillMaxSize().background(MsCrimson),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(72.dp).scale(scale).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = MsCrimson)
        }

        Spacer(Modifier.height(20.dp))
        Text("Sab set ho gaya!", style = MsType.headlineMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "${capWords(state.name)} bhai, MaalSaathi family mein\naapka swagat hai! 🎉",
            fontSize = 18.sp, color = Color.White.copy(0.9f), textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Text("Pehli trip shuru karte hain!", fontSize = 16.sp, color = Color.White.copy(0.7f))
    }
}
