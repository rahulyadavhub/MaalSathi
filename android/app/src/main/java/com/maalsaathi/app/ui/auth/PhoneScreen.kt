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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.UserRole
import com.maalsaathi.app.ui.common.*

@Composable
fun PhoneScreen(viewModel: AuthViewModel, onResult: (PhoneResult) -> Unit) {
    val state by viewModel.state.collectAsState()
    val c = Ms.colors
    val focusReq = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focusReq.requestFocus() }

    Column(Modifier.fillMaxSize().background(c.background).imePadding()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = Sp.screenHPad), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Text("🚛", fontSize = 40.sp)
            Text("MaalSaathi", style = MsType.headlineMedium)
            Spacer(Modifier.height(32.dp))
            Text("Apna number daalo", style = MsType.headlineLarge)
            Spacer(Modifier.height(4.dp))
            Text("WhatsApp wala number daalo", style = MsType.bodyMedium)
            Spacer(Modifier.height(24.dp))

            Surface(shape = RoundedCornerShape(Sp.cardRadius), color = c.card,
                border = BorderStroke(if (isFocused) 2.dp else Sp.cardBorder, if (state.error != null) c.loss else if (isFocused) c.primary else c.border)) {
                Column(Modifier.padding(Sp.cardPad)) {
                    Text("Mobile Number", style = MsType.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+91", fontSize = 20.sp, fontFamily = Poppins, color = c.textSecondary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.width(1.dp).height(28.dp).background(c.border))
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(value = state.phone, onValueChange = { viewModel.updatePhone(it) },
                            modifier = Modifier.weight(1f).focusRequester(focusReq).onFocusChanged { isFocused = it.isFocused },
                            textStyle = TextStyle(fontSize = 20.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (state.phone.length == 10) viewModel.checkPhone(onResult) }),
                            singleLine = true, cursorBrush = SolidColor(c.primary),
                            decorationBox = { inner -> if (state.phone.isEmpty()) Text("9876543210", fontSize = 20.sp, fontFamily = Poppins, color = c.textSecondary.copy(0.4f), fontWeight = FontWeight.Bold); inner() })
                    }
                }
            }

            if (state.error != null) { Spacer(Modifier.height(8.dp)); Text(state.error!!, style = MsType.bodyMedium.copy(color = c.loss)) }

            Spacer(Modifier.height(20.dp))
            Text("Aap kaun hain?", style = MsType.labelLarge, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            RoleCard("🏢", "Maalik hun", "2 ya zyada trucks hain mere paas", state.role == UserRole.MAALIK, c.textPrimary) { viewModel.updateRole(UserRole.MAALIK) }
            Spacer(Modifier.height(8.dp))
            RoleCard("🚛💼", "Maalik-Driver hun", "Akela hun, khud hi chalata hun", state.role == UserRole.MAALIK_DRIVER, c.primary) { viewModel.updateRole(UserRole.MAALIK_DRIVER) }
            Spacer(Modifier.height(8.dp))
            RoleCard("👨‍✈️", "Driver hun", "Kisi ke under kaam karta hun", state.role == UserRole.DRIVER, Color(0xFF64748B)) { viewModel.updateRole(UserRole.DRIVER) }

            if (state.role == UserRole.DRIVER) {
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = c.primary.copy(0.08f)) {
                    Text("ℹ️ Owner ka invite link ya code chahiye", style = MsType.labelLarge.copy(color = c.primary), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = Sp.screenHPad, vertical = 12.dp)) {
            MsPrimaryButton(text = if (state.isLoading) "Dekh raha hoon..." else "Aage Badho →", onClick = { viewModel.checkPhone(onResult) }, enabled = state.phone.length == 10 && !state.isLoading, loading = state.isLoading)
        }
    }
}

@Composable
private fun RoleCard(emoji: String, title: String, subtitle: String, selected: Boolean, borderColor: Color, onClick: () -> Unit) {
    val c = Ms.colors
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(Sp.cardRadius),
        color = if (selected) borderColor.copy(0.06f) else c.card,
        border = BorderStroke(if (selected) 2.dp else Sp.cardBorder, if (selected) borderColor else c.border)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MsType.titleMedium, color = if (selected) borderColor else c.textPrimary)
                Text(subtitle, style = MsType.bodyMedium)
            }
            if (selected) Text("✓", fontSize = 18.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = borderColor)
        }
    }
}
