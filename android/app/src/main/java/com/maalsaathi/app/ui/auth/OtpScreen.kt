package com.maalsaathi.app.ui.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(viewModel: AuthViewModel, onBack: () -> Unit, onVerified: (isNewUser: Boolean) -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val otpFull = state.otp.all { it.isNotEmpty() }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
        viewModel.startResendCountdown()
        while (true) { delay(1000); viewModel.tickCountdown() }
    }

    // Auto-submit when all 6 digits filled
    LaunchedEffect(otpFull) {
        if (otpFull) viewModel.verifyOtp(onVerified)
    }

    Column(Modifier.fillMaxSize().background(c.background).imePadding()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wapas", tint = c.textPrimary)
        }

        Column(
            Modifier.weight(1f).padding(horizontal = Sp.screenHPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text("OTP aaya?", style = MsType.headlineMedium, color = c.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("+91 ${state.phone} pe SMS bheja", fontSize = 16.sp, color = c.textSecondary)
            TextButton(onClick = onBack) {
                Text("${state.phone} badlo", fontSize = 14.sp, color = c.primary)
            }

            Spacer(Modifier.height(32.dp))

            // OTP boxes
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                state.otp.forEachIndexed { index, digit ->
                    if (index > 0) Spacer(Modifier.width(8.dp))
                    val hasError = state.otpError != null
                    val filled = digit.isNotEmpty()
                    Surface(
                        modifier = Modifier.size(width = 48.dp, height = 56.dp),
                        shape = RoundedCornerShape(10.dp), color = if (filled) c.primary.copy(0.06f) else c.card,
                        border = BorderStroke(if (filled) 1.dp else 1.dp, if (hasError) c.loss else if (filled) c.primary else c.border),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            BasicTextField(
                                value = digit, onValueChange = { v ->
                                    val d = v.filter { it.isDigit() }.take(1)
                                    viewModel.updateOtpDigit(index, d)
                                    if (d.isNotEmpty() && index < 5) focusRequesters[index + 1].requestFocus()
                                },
                                modifier = Modifier.size(width = 48.dp, height = 56.dp).focusRequester(focusRequesters[index]),
                                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                cursorBrush = SolidColor(c.primary),
                            )
                        }
                    }
                }
            }

            if (state.otpError != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.otpError!!, fontSize = 14.sp, color = c.loss)
            }

            Spacer(Modifier.height(20.dp))
            Text("OTP nahi aaya?", fontSize = 14.sp, color = c.textSecondary)
            if (state.resendCountdown > 0) {
                Text("${state.resendCountdown} second mein dobara bhejo", fontSize = 14.sp, color = c.textSecondary)
            } else {
                TextButton(onClick = { viewModel.resendOtp() }) {
                    Text("Dobara Bhejo", fontSize = 14.sp, fontFamily = Poppins, color = c.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 16.dp)) {
            MsPrimaryButton(
                text = if (state.isLoading) "Check kar raha hoon..." else "Verify Karo ✓",
                onClick = { viewModel.verifyOtp(onVerified) },
                enabled = otpFull && !state.isLoading,
                loading = state.isLoading,
            )
        }
    }
}
