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
fun OrganizationSetupScreen(viewModel: AuthViewModel, onNext: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors
    val focus = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize().background(c.background).imePadding()) {
        Column(Modifier.weight(1f).padding(horizontal = Sp.screenHPad), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text("🏢", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Aapki company ka naam?", style = MsType.headlineLarge)
            Spacer(Modifier.height(4.dp))
            Text("Fleet manage karne ke liye", style = MsType.bodyMedium)
            Spacer(Modifier.height(32.dp))

            Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                border = BorderStroke(if (isFocused) 2.dp else Sp.cardBorder, if (isFocused) c.primary else c.border)) {
                Column(Modifier.padding(Sp.cardPad)) {
                    Text("Company / Transport Name", style = MsType.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = state.organizationName, onValueChange = { viewModel.updateOrganizationName(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(fontSize = 20.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (state.organizationName.trim().length >= 2) viewModel.setupOrganization(onNext) }),
                        singleLine = true, cursorBrush = SolidColor(c.primary),
                        decorationBox = { inner ->
                            if (state.organizationName.isEmpty()) Text("Jaise: Sharma Transport", fontSize = 20.sp, fontFamily = Poppins, color = c.textSecondary.copy(0.4f))
                            inner()
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            MsTextButton("Sirf aap use karenge? Seedha truck daalo →", onClick = { viewModel.setupOrganization(onNext) })
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 16.dp)) {
            MsPrimaryButton("Aage →", onClick = { viewModel.setupOrganization(onNext) },
                enabled = state.organizationName.trim().length >= 2 && !state.isLoading, loading = state.isLoading)
        }
    }
}
