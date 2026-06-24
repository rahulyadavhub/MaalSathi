package com.maalsaathi.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun AddFirstTruckScreen(viewModel: AuthViewModel, onDone: () -> Unit, onSkip: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors
    val focus = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize().background(c.background).imePadding()) {
        Column(Modifier.weight(1f).padding(horizontal = Sp.screenHPad), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text("🚛", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Pehla truck add karo", style = MsType.headlineLarge)
            Spacer(Modifier.height(4.dp))
            Text("Baad mein aur bhi add kar sakte ho", style = MsType.bodyMedium)
            Spacer(Modifier.height(32.dp))

            Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                border = BorderStroke(if (isFocused) 2.dp else Sp.cardBorder, if (isFocused) c.primary else c.border)) {
                Column(Modifier.padding(Sp.cardPad)) {
                    Text("Truck Number", style = MsType.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = state.truckNumber, onValueChange = { viewModel.updateTruckNumber(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(fontSize = 20.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.addFirstTruck(onDone) }),
                        singleLine = true, cursorBrush = SolidColor(c.primary),
                        decorationBox = { inner ->
                            if (state.truckNumber.isEmpty()) Text("MH12AB1234", fontSize = 20.sp, fontFamily = Poppins, color = c.textSecondary.copy(0.4f), fontWeight = FontWeight.Bold)
                            inner()
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Image required toggle
            Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card, border = BorderStroke(Sp.cardBorder, c.border)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Driver ko image dalni padegi?", style = MsType.titleMedium)
                        Text("Diesel, toll — sab ka photo lena padega", style = MsType.labelSmall)
                    }
                    Switch(checked = state.imageRequired, onCheckedChange = { viewModel.updateImageRequired(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = c.primary, checkedThumbColor = c.card))
                }
            }

            Spacer(Modifier.height(12.dp))
            MsTextButton("Baad mein add karunga →", onClick = onSkip)
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 16.dp)) {
            MsPrimaryButton("Truck Add Karo ✓", onClick = { viewModel.addFirstTruck(onDone) },
                enabled = state.truckNumber.trim().length >= 4 && !state.isLoading, loading = state.isLoading)
        }
    }
}
