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
fun NameScreen(viewModel: AuthViewModel, onNext: () -> Unit) {
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
            StepDots(current = 0, total = 2)
            Spacer(Modifier.height(32.dp))
            Text("👤", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Aapka naam kya hai?", style = MsType.headlineMedium, color = c.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Trips aur hisaab mein dikhega", fontSize = 16.sp, color = c.textSecondary)
            Spacer(Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                border = BorderStroke(if (isFocused) 2.dp else 1.dp, if (isFocused) c.primary else c.border),
            ) {
                Column(Modifier.padding(Sp.cardPad)) {
                    Text("Naam", fontSize = 14.sp, color = c.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = state.name, onValueChange = { viewModel.updateName(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, color = c.textPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (state.name.length >= 2) onNext() }),
                        singleLine = true, cursorBrush = SolidColor(c.primary),
                        decorationBox = { inner ->
                            if (state.name.isEmpty()) Text("Jaise: Ramesh Kumar", fontSize = 20.sp, color = c.textSecondary.copy(0.4f))
                            inner()
                        },
                    )
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 16.dp)) {
            MsPrimaryButton("Aage →", onClick = onNext, enabled = state.name.trim().length >= 2)
        }
    }
}

@Composable
fun StepDots(current: Int, total: Int) {
    val c = Ms.colors
    androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Step ${current + 1} of $total", fontSize = 13.sp, fontFamily = NotoSans, color = c.textSecondary)
        Spacer(Modifier.height(4.dp))
        repeat(total) { i ->
            androidx.compose.foundation.layout.Box(
                Modifier.height(6.dp).then(if (i <= current) Modifier.fillMaxWidth(0.15f) else Modifier.fillMaxWidth(0.08f))
                    .background(if (i <= current) c.primary else c.border, RoundedCornerShape(3.dp))
            )
        }
    }
}
