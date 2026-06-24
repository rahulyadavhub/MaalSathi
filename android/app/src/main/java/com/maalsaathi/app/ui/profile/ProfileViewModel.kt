package com.maalsaathi.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maalsaathi.app.data.models.UserRole
import com.maalsaathi.app.ui.auth.AuthPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.MAALIK_DRIVER,
    val truckNumber: String = "",
    val companyName: String = "Sharma Transport",
    val driverCount: Int = 3,
    val truckCount: Int = 4,
    val monthlyEarnings: Long = 340_000,
    val totalTrips: Int = 24,
    val totalEarnings: Long = 1_240_000,
    val ownerName: String = "Rahul Yadav",
    val ownerPhone: String = "9876543210",
    val imageRequired: Boolean = true,
    val joinedDate: String = "June 2026",
    val subscriptionPlan: String = "free",
    val subscriptionExpiry: String = "30 July 2026",
    val selectedLanguage: String = "hinglish",
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
    val isEditingName: Boolean = false,
    val isEditingTruck: Boolean = false,
)

class ProfileViewModel(private val prefs: AuthPreferences) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init { loadProfile() }

    private fun loadProfile() {
        _state.update {
            it.copy(
                name = prefs.userName.ifBlank { "Driver" },
                phone = prefs.userId.removePrefix("user_"),
                role = prefs.userRoleEnum,
                truckNumber = prefs.truckNumber,
            )
        }
    }

    fun startEditName() { _state.update { it.copy(isEditingName = true) } }
    fun cancelEditName() { _state.update { it.copy(isEditingName = false, name = prefs.userName) } }
    fun saveEditName() { prefs.userName = _state.value.name; _state.update { it.copy(isEditingName = false) } }
    fun updateName(v: String) { if (v.length <= 50) _state.update { it.copy(name = v) } }

    fun startEditTruck() { _state.update { it.copy(isEditingTruck = true) } }
    fun cancelEditTruck() { _state.update { it.copy(isEditingTruck = false, truckNumber = prefs.truckNumber) } }
    fun saveEditTruck() { prefs.truckNumber = _state.value.truckNumber; _state.update { it.copy(isEditingTruck = false) } }
    fun updateTruck(v: String) { if (v.length <= 15) _state.update { it.copy(truckNumber = v.uppercase()) } }

    fun selectLanguage(lang: String) { _state.update { it.copy(selectedLanguage = lang) } }
    fun toggleNotifications() { _state.update { it.copy(notificationsEnabled = !it.notificationsEnabled) } }

    fun logout() { prefs.logout() }

    class Factory(private val prefs: AuthPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(prefs) as T
    }
}
