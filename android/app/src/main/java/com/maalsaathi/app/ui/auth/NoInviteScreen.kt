package com.maalsaathi.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*

@Composable
fun NoInviteScreen(viewModel: AuthViewModel, onCodeAccepted: () -> Unit, onSwitchRole: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors

    Column(Modifier.fillMaxSize().background(c.background).imePadding().padding(horizontal = Sp.screenHPad), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(80.dp))
        Text("🔗", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Invite Link Chahiye", style = MsType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Apne owner se MaalSaathi ka invite link maango", style = MsType.bodyMedium, textAlign = TextAlign.Center)

        Spacer(Modifier.height(32.dp))

        Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card, border = BorderStroke(Sp.cardBorder, c.border)) {
            Column(Modifier.padding(Sp.cardPad)) {
                Text("Ya invite code daalo", style = MsType.labelLarge)
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = state.inviteCode, onValueChange = { viewModel.updateInviteCode(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 24.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary, textAlign = TextAlign.Center, letterSpacing = 4.sp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true, cursorBrush = SolidColor(c.primary),
                    decorationBox = { inner ->
                        if (state.inviteCode.isEmpty()) Text("ABC123", fontSize = 24.sp, fontFamily = Poppins, color = c.textSecondary.copy(0.3f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, letterSpacing = 4.sp, modifier = Modifier.fillMaxWidth())
                        inner()
                    },
                )
            }
        }

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, style = MsType.bodyMedium.copy(color = c.loss))
        }

        Spacer(Modifier.height(16.dp))
        MsPrimaryButton("Code Se Join Karo", onClick = { viewModel.acceptInviteByCode(onCodeAccepted, {}) },
            enabled = state.inviteCode.length == 6 && !state.isLoading, loading = state.isLoading)

        Spacer(Modifier.weight(1f))
        MsTextButton("Khud truck chalate ho? Maalik-Driver bano →", onClick = onSwitchRole)
        Spacer(Modifier.height(16.dp))
    }
}
