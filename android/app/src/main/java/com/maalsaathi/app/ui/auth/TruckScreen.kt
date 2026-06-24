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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*

@Composable
fun TruckScreen(viewModel: AuthViewModel, onDone: () -> Unit, onSkip: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors
    val focus = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize().background(c.background).imePadding()) {
        Column(
            Modifier.weight(1f).padding(horizontal = Sp.screenHPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            StepDots(current = 1, total = 2)
            Spacer(Modifier.height(32.dp))
            Text("🚛", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Gaadi ka number?", style = MsType.headlineMedium, color = c.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Jaise: MH 12 AB 1234", fontSize = 16.sp, color = c.textSecondary)
            Spacer(Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                border = BorderStroke(if (isFocused) 2.dp else 1.dp, if (isFocused) c.primary else c.border),
            ) {
                Column(Modifier.padding(Sp.cardPad)) {
                    Text("Truck Number", fontSize = 14.sp, color = c.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = state.truckNumber, onValueChange = { viewModel.updateTruckNumber(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.registerUser(onDone) }),
                        singleLine = true, cursorBrush = SolidColor(c.primary),
                        decorationBox = { inner ->
                            if (state.truckNumber.isEmpty()) Text("MH12AB1234", fontSize = 20.sp, fontFamily = Poppins, color = c.textSecondary.copy(0.4f), fontWeight = FontWeight.Bold)
                            inner()
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSkip) {
                Text("Truck number nahi pata? Skip karo", fontSize = 14.sp, color = c.textSecondary)
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 16.dp)) {
            MsPrimaryButton(
                text = if (state.isLoading) "Register ho raha hai..." else "Shuru Karo ✓",
                onClick = { viewModel.registerUser(onDone) },
                loading = state.isLoading,
            )
        }
    }
}
