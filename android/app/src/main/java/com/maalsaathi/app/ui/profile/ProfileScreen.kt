package com.maalsaathi.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.UserRole
import com.maalsaathi.app.ui.common.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit, onManageFleet: () -> Unit) {
    val state by viewModel.state.collectAsState()
    when (state.role) {
        UserRole.MAALIK -> MaalikProfileContent(viewModel, state, onLogout, onManageFleet)
        UserRole.DRIVER -> DriverProfileContent(viewModel, state, onLogout)
        UserRole.MAALIK_DRIVER -> MaalikDriverProfileContent(viewModel, state, onLogout)
    }
}

// ═══════════════════════════════════════════════════
// MAALIK PROFILE
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaalikProfileContent(vm: ProfileViewModel, s: ProfileUiState, onLogout: () -> Unit, onManageFleet: () -> Unit) {
    val c = Ms.colors; val ctx = LocalContext.current
    var showLang by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(24.dp)); Text("Profile", style = MsType.headlineLarge) }

            item { HeroCard(s, vm, scope, snackbar, accentBar = true) }

            // Fleet overview
            item {
                MsCard {
                    Text("Aapki Fleet", style = MsType.titleMedium.copy(color = c.primary))
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        StatBox("Trucks", "${s.truckCount}", Modifier.weight(1f))
                        StatBox("Drivers", "${s.driverCount}", Modifier.weight(1f))
                        StatBox("Is Mahine", formatRupees(s.monthlyEarnings), Modifier.weight(1f), c.profit)
                    }
                    Spacer(Modifier.height(10.dp))
                    ProfileRow("🚛", "Trucks Manage Karo", "${s.truckCount} trucks", onClick = onManageFleet)
                }
            }

            // Subscription
            item {
                MsCard {
                    Text("Aapka Plan", style = MsType.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (s.subscriptionPlan == "free") {
                        Text("FREE Plan", style = MsType.headlineMedium.copy(color = c.textSecondary))
                        Text("10 trips/month · Basic features", style = MsType.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                        MsPrimaryButton("PRO Plan Lelo — ₹199/month", onClick = { /* TODO */ })
                    } else {
                        Text("PRO Plan ✓", style = MsType.headlineMedium.copy(color = c.primary))
                        Text("Valid till: ${s.subscriptionExpiry}", style = MsType.bodyMedium)
                    }
                }
            }

            item { SettingsCard(s, vm, onShowLang = { showLang = true }) }
            item { SupportCard(ctx) }
            item { DangerZone(onLogout = { showLogout = true }) }
            item { Spacer(Modifier.height(20.dp)) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showLang) LanguageSheet(s.selectedLanguage, { vm.selectLanguage(it); showLang = false }, { showLang = false })
    if (showLogout) LogoutSheet({ vm.logout(); onLogout() }, { showLogout = false })
}

// ═══════════════════════════════════════════════════
// DRIVER PROFILE
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverProfileContent(vm: ProfileViewModel, s: ProfileUiState, onLogout: () -> Unit) {
    val c = Ms.colors; val ctx = LocalContext.current
    var showLang by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(24.dp)); Text("Mera Profile", style = MsType.headlineLarge) }

            item { HeroCard(s, vm, scope, snackbar, accentBar = true) }

            // Truck info — read only
            item {
                MsCard {
                    Text("Meri Truck", style = MsType.titleMedium.copy(color = c.primary))
                    Spacer(Modifier.height(8.dp))
                    LockedRow("🚛", "Truck Number", s.truckNumber.ifBlank { "MH12AB1234" })
                    ThinDivider()
                    LockedRow("🏢", "Company", capWords(s.companyName))
                    ThinDivider()
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📷", fontSize = 18.sp); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Photo Zaroori?", style = MsType.labelLarge)
                            if (s.imageRequired) { Text("Haan ✓", style = MsType.titleMedium.copy(color = c.primary)); Text("Owner ne set kiya hai", style = MsType.labelSmall) }
                            else { Text("Nahi", style = MsType.titleMedium); Text("Optional hai", style = MsType.labelSmall) }
                        }
                    }
                    ThinDivider()
                    LockedRow("📅", "Joined", s.joinedDate)
                }
            }

            // Owner contact
            item {
                MsCard {
                    Text("Mera Owner", style = MsType.titleMedium.copy(color = c.primary))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(c.primary), contentAlignment = Alignment.Center) {
                            Text(s.ownerName.firstOrNull()?.uppercase() ?: "?", fontSize = 20.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.onPrimary)
                        }
                        Column { Text(capWords(s.ownerName), style = MsType.titleMedium); Text(capWords(s.companyName), style = MsType.bodyMedium) }
                    }
                    Spacer(Modifier.height(10.dp))
                    MsDestructiveButton("📞 Call Karo", onClick = { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${s.ownerPhone}"))) })
                    Spacer(Modifier.height(6.dp))
                    MsSecondaryButton("💬 WhatsApp Karo", onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${s.ownerPhone}"))) })
                }
            }

            item { SettingsCard(s, vm, onShowLang = { showLang = true }) }
            item { SupportCard(ctx) }
            item {
                DangerZone(onLogout = { showLogout = true })
                Text("Account delete ke liye apne owner se baat karo", style = MsType.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showLang) LanguageSheet(s.selectedLanguage, { vm.selectLanguage(it); showLang = false }, { showLang = false })
    if (showLogout) LogoutSheet({ vm.logout(); onLogout() }, { showLogout = false })
}

// ═══════════════════════════════════════════════════
// MAALIK-DRIVER PROFILE
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaalikDriverProfileContent(vm: ProfileViewModel, s: ProfileUiState, onLogout: () -> Unit) {
    val c = Ms.colors; val ctx = LocalContext.current
    var showLang by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = Sp.screenHPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(24.dp)); Text("Profile", style = MsType.headlineLarge) }

            item { HeroCard(s, vm, scope, snackbar, accentBar = false) }

            // Gaadi info — editable truck
            item {
                MsCard {
                    Text("Meri Gaadi", style = MsType.titleMedium.copy(color = c.primary))
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🚛", fontSize = 18.sp); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Truck Number", style = MsType.labelLarge)
                            if (s.isEditingTruck) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicTextField(value = s.truckNumber, onValueChange = { vm.updateTruck(it) }, modifier = Modifier.weight(1f),
                                        textStyle = TextStyle(fontSize = 18.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary), singleLine = true, cursorBrush = SolidColor(c.primary))
                                    Surface(onClick = { vm.saveEditTruck(); scope.launch { snackbar.showSnackbar("Truck number save ✓") } }, shape = RoundedCornerShape(6.dp), color = c.primary) {
                                        Text("✓", style = MsType.labelLarge.copy(color = c.onPrimary), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                    }
                                }
                            } else {
                                Text(s.truckNumber.ifBlank { "—" }, style = MsType.titleMedium)
                            }
                        }
                        if (!s.isEditingTruck) IconButton(onClick = { vm.startEditTruck() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = c.textSecondary, modifier = Modifier.size(18.dp)) }
                    }
                    ThinDivider()
                    LockedRow("📅", "Member Since", s.joinedDate)
                    ThinDivider()
                    LockedRow("📊", "Total Trips", "${s.totalTrips} trips")
                    ThinDivider()
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💰", fontSize = 18.sp); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text("Total Kamaai", style = MsType.labelLarge); Text(formatRupees(s.totalEarnings), style = MsType.titleMedium.copy(color = c.profit)) }
                    }
                }
            }

            // Subscription
            item {
                MsCard {
                    Text("Aapka Plan", style = MsType.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("FREE Plan", style = MsType.headlineMedium.copy(color = c.textSecondary))
                    Text("10 trips/month · Basic features", style = MsType.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    MsPrimaryButton("PRO Plan Lelo — ₹199/month", onClick = { /* TODO */ })
                }
            }

            item { SettingsCard(s, vm, onShowLang = { showLang = true }) }
            item { SupportCard(ctx) }
            item { DangerZone(onLogout = { showLogout = true }) }
            item { Spacer(Modifier.height(20.dp)) }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showLang) LanguageSheet(s.selectedLanguage, { vm.selectLanguage(it); showLang = false }, { showLang = false })
    if (showLogout) LogoutSheet({ vm.logout(); onLogout() }, { showLogout = false })
}

// ═══════════════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════════════

@Composable
private fun HeroCard(s: ProfileUiState, vm: ProfileViewModel, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState, accentBar: Boolean) {
    val c = Ms.colors
    val masked = if (s.phone.length >= 10) "+91 ${s.phone.take(2)}XXX XX${s.phone.takeLast(3)}" else s.phone
    val roleLabel = when (s.role) { UserRole.MAALIK -> "Maalik"; UserRole.DRIVER -> "Driver"; UserRole.MAALIK_DRIVER -> "Maalik-Driver" }
    val initials = s.name.split(" ").take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "?" }

    MsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(c.primary), contentAlignment = Alignment.Center) {
                Text(initials, fontSize = 28.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.onPrimary)
            }
            Column(Modifier.weight(1f)) {
                if (s.isEditingName) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(value = s.name, onValueChange = { vm.updateName(it) }, modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 20.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = c.textPrimary), singleLine = true, cursorBrush = SolidColor(c.primary))
                        Surface(onClick = { vm.saveEditName(); scope.launch { snackbar.showSnackbar("Naam update ho gaya ✓") } }, shape = RoundedCornerShape(6.dp), color = c.primary) {
                            Text("✓", style = MsType.labelLarge.copy(color = c.onPrimary), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(capWords(s.name), style = MsType.headlineMedium, modifier = Modifier.weight(1f, false))
                        IconButton(onClick = { vm.startEditName() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, "Edit", tint = c.textSecondary, modifier = Modifier.size(16.dp)) }
                    }
                }
                Text(masked, style = MsType.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = c.primary) {
                    Text(roleLabel, style = MsType.labelSmall.copy(color = c.onPrimary, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                if (s.role == UserRole.MAALIK) { Spacer(Modifier.height(2.dp)); Text(capWords(s.companyName), style = MsType.labelLarge) }
                if (s.role == UserRole.DRIVER) { Spacer(Modifier.height(2.dp)); Text(s.truckNumber.ifBlank { "MH12AB1234" }, style = MsType.labelLarge) }
            }
        }
    }
}

@Composable
private fun SettingsCard(s: ProfileUiState, vm: ProfileViewModel, onShowLang: () -> Unit) {
    val c = Ms.colors
    MsCard {
        Text("Settings", style = MsType.titleMedium)
        Spacer(Modifier.height(8.dp))
        ProfileRow("🌐", "Bhasha", s.selectedLanguage.replaceFirstChar { it.uppercase() }, onClick = onShowLang)
        ThinDivider()
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔔", fontSize = 18.sp); Spacer(Modifier.width(12.dp))
            Text("Notifications", style = MsType.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = s.notificationsEnabled, onCheckedChange = { vm.toggleNotifications() }, colors = SwitchDefaults.colors(checkedTrackColor = c.primary, checkedThumbColor = c.card))
        }
        ThinDivider()
        ProfileRow("📱", "App Version", "v${s.appVersion} (Beta)")
    }
}

@Composable
private fun SupportCard(ctx: android.content.Context) {
    MsCard {
        ProfileRow("💬", "MaalSaathi Support", "WhatsApp pe baat karo", onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918879688678"))) })
        ThinDivider()
        ProfileRow("📤", "Dosto Ko Batao", "App share karo", onClick = {
            ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "MaalSaathi — Truck owners ka digital munshi! Download karo: https://maalsaathi.app") }, "Share"))
        })
    }
}

@Composable
private fun DangerZone(onLogout: () -> Unit) {
    Surface(onClick = onLogout, modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = androidx.compose.ui.graphics.Color.Black.copy(0.08f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = Ms.colors.card) {
        Row(Modifier.fillMaxWidth().padding(Sp.cardPad), verticalAlignment = Alignment.CenterVertically) {
            Text("🚪", fontSize = 18.sp); Spacer(Modifier.width(12.dp)); Text("Logout", style = MsType.titleMedium.copy(color = Ms.colors.loss))
        }
    }
}

@Composable
private fun ProfileRow(emoji: String, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).let { if (onClick != null) it.background(androidx.compose.ui.graphics.Color.Transparent) else it }, verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 18.sp); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(label, style = MsType.labelLarge); Text(value, style = MsType.titleMedium) }
        if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Ms.colors.textSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LockedRow(emoji: String, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 18.sp); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(label, style = MsType.labelLarge); Text(value, style = MsType.titleMedium) }
        Icon(Icons.Default.Lock, null, tint = Ms.colors.textSecondary.copy(0.4f), modifier = Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Ms.colors.card) {
        Column(Modifier.padding(20.dp)) {
            Text("Bhasha Chuniye", style = MsType.titleLarge)
            Spacer(Modifier.height(12.dp))
            listOf("hindi" to "हिंदी — Puri tarah Hindi", "hinglish" to "Hinglish — Hindi + English mix", "english" to "English — Full English").forEach { (key, label) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = current == key, onClick = { onSelect(key) }, colors = RadioButtonDefaults.colors(selectedColor = Ms.colors.primary))
                    Spacer(Modifier.width(8.dp)); Text(label, style = MsType.bodyLarge)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Ms.colors.card) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🚪", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("Pakka logout karna hai?", style = MsType.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Aapka data safe hai — dobara login karke wapas aa sakte hain", style = MsType.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            MsPrimaryButton("Haan, Logout Karo", onClick = onConfirm)
            Spacer(Modifier.height(8.dp))
            MsSecondaryButton("Nahi, Ruko", onClick = onDismiss)
            Spacer(Modifier.height(8.dp))
        }
    }
}
