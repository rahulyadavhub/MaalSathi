package com.maalsaathi.app.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.MsCrimson
import com.maalsaathi.app.ui.common.MsType
import com.maalsaathi.app.ui.common.Poppins
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (isLoggedIn: Boolean) -> Unit, isLoggedIn: Boolean) {
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f, animationSpec = tween(400), label = "text",
    )

    LaunchedEffect(Unit) {
        delay(200); showLogo = true
        delay(400); showText = true
        delay(1800); onNavigate(isLoggedIn)
    }

    Box(Modifier.fillMaxSize().background(MsCrimson), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🚛", fontSize = 80.sp, modifier = Modifier.scale(logoScale))
            Spacer(Modifier.height(16.dp))
            Text("MaalSaathi", style = MsType.displayLarge.copy(color = Color.White, fontFamily = com.maalsaathi.app.ui.common.Poppins), modifier = Modifier.graphicsLayer { alpha = textAlpha })
            Spacer(Modifier.height(4.dp))
            Text("Apna Digital Munshi", style = MsType.bodyMedium.copy(color = Color.White.copy(0.7f)), modifier = Modifier.graphicsLayer { alpha = textAlpha })
            Spacer(Modifier.height(48.dp))
            LoadingDotsWhite()
        }
    }
}

@Composable
private fun LoadingDotsWhite() {
    val anim = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val alpha by anim.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500, delayMillis = i * 150), RepeatMode.Reverse), label = "d$i")
            Box(Modifier.size(10.dp).graphicsLayer { this.alpha = alpha }.clip(CircleShape).background(Color.White))
        }
    }
}
