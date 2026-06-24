package com.maalsaathi.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maalsaathi.app.data.models.UserRole
import com.maalsaathi.app.data.repository.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.maalsaathi.app.data.models.Organization
import com.maalsaathi.app.data.models.Truck
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

enum class PhoneResult { WHATSAPP_USER, EXISTING_USER, NEW_USER }

data class AuthUiState(
    val isLoading: Boolean = false,
    val phone: String = "",
    val role: UserRole = UserRole.MAALIK_DRIVER,
    val name: String = "",
    val truckNumber: String = "",
    val organizationName: String = "",
    val inviteCode: String = "",
    val otp: List<String> = List(6) { "" },
    val otpSent: Boolean = false,
    val phoneResult: PhoneResult? = null,
    val isWhatsappUser: Boolean = false,
    val existingUserName: String = "",
    val existingUserStats: ExistingUserStats? = null,
    val error: String? = null,
    val otpError: String? = null,
    val resendCountdown: Int = 0,
    val imageRequired: Boolean = false,
)

data class ExistingUserStats(
    val trips: Int = 12,
    val totalEarnings: Long = 340_000,
    val thisMonth: Long = 85_000,
)

class AuthViewModel(
    private val prefs: AuthPreferences,
    private val fleetRepo: FleetRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun isLoggedIn(): Boolean = prefs.isLoggedIn
    fun getUserRole(): UserRole = prefs.userRoleEnum

    fun updatePhone(v: String) {
        if (v.length <= 10 && v.all { it.isDigit() }) _state.update { it.copy(phone = v, error = null) }
    }
    fun updateRole(role: UserRole) { _state.update { it.copy(role = role) } }
    fun updateName(v: String) { if (v.length <= 50) _state.update { it.copy(name = v) } }
    fun updateTruckNumber(v: String) { if (v.length <= 15) _state.update { it.copy(truckNumber = v.uppercase()) } }
    fun updateOrganizationName(v: String) { if (v.length <= 60) _state.update { it.copy(organizationName = v) } }
    fun updateInviteCode(v: String) { if (v.length <= 6) _state.update { it.copy(inviteCode = v.uppercase()) } }
    fun updateImageRequired(v: Boolean) { _state.update { it.copy(imageRequired = v) } }

    fun updateOtpDigit(index: Int, digit: String) {
        val newOtp = _state.value.otp.toMutableList()
        newOtp[index] = digit.take(1)
        _state.update { it.copy(otp = newOtp, otpError = null) }
    }

    fun clearOtp() { _state.update { it.copy(otp = List(6) { "" }, otpError = null) } }

    fun checkPhone(onResult: (PhoneResult) -> Unit) {
        val phone = _state.value.phone
        if (phone.length != 10) { _state.update { it.copy(error = "Sahi number daalo — 10 digit ka") }; return }
        _state.update { it.copy(isLoading = true, error = null) }

        // TODO: POST /api/auth/check-phone { phone: "91$phone" }
        val result = when {
            phone.startsWith("99") -> {
                _state.update { it.copy(isWhatsappUser = true, existingUserName = "Ramesh", existingUserStats = ExistingUserStats(), phoneResult = PhoneResult.WHATSAPP_USER) }
                PhoneResult.WHATSAPP_USER
            }
            phone.startsWith("88") -> {
                _state.update { it.copy(phoneResult = PhoneResult.EXISTING_USER, existingUserName = "Suresh") }
                PhoneResult.EXISTING_USER
            }
            else -> {
                _state.update { it.copy(phoneResult = PhoneResult.NEW_USER) }
                PhoneResult.NEW_USER
            }
        }
        _state.update { it.copy(isLoading = false, otpSent = result != PhoneResult.WHATSAPP_USER) }
        onResult(result)
    }

    fun verifyOtp(onSuccess: (isNewUser: Boolean) -> Unit) {
        val otp = _state.value.otp.joinToString("")
        if (otp.length != 6) return
        _state.update { it.copy(isLoading = true, otpError = null) }

        // TODO: POST /api/auth/verify-otp { phone, otp }
        if (otp == "123456") {
            _state.update { it.copy(isLoading = false) }
            val isNew = _state.value.phoneResult == PhoneResult.NEW_USER
            if (!isNew) saveAuth(name = _state.value.existingUserName, truck = "")
            onSuccess(isNew)
        } else {
            _state.update { it.copy(isLoading = false, otpError = "Galat OTP hai, dobara daalo") }
        }
    }

    fun setupOrganization(onDone: () -> Unit) {
        val s = _state.value
        val orgName = s.organizationName.trim().ifBlank { s.name.trim() + " Transport" }
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val orgId = "org1"
            val org = Organization(orgId, "user_${s.phone}", orgName, s.name.trim(), s.phone)
            fleetRepo?.createOrganization(org)
            prefs.organizationId = orgId
            prefs.userRole = UserRole.MAALIK.name
            _state.update { it.copy(isLoading = false, organizationName = orgName) }
            onDone()
        }
    }

    fun addFirstTruck(onDone: () -> Unit) {
        val s = _state.value
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val truck = Truck(
                id = UUID.randomUUID().toString(),
                organizationId = prefs.organizationId,
                registrationNumber = s.truckNumber.uppercase(),
                imageRequired = s.imageRequired,
            )
            fleetRepo?.addTruck(truck)
            saveAuth(name = s.name, truck = s.truckNumber)
            _state.update { it.copy(isLoading = false) }
            onDone()
        }
    }

    fun acceptInviteByCode(onSuccess: () -> Unit, onError: () -> Unit) {
        val code = _state.value.inviteCode
        if (code.length != 6) { _state.update { it.copy(error = "6 digit ka code daalo") }; return }
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val invite = fleetRepo?.getInviteByCode(code)
            if (invite != null && invite.status == com.maalsaathi.app.data.models.InviteStatus.PENDING) {
                val driverId = "user_${_state.value.phone}"
                fleetRepo?.acceptInvite(code, driverId, _state.value.name.trim())
                prefs.assignedTruckId = invite.truckId
                prefs.organizationId = invite.organizationId
                _state.update { it.copy(isLoading = false, truckNumber = invite.truckNumber) }
                saveAuth(name = _state.value.name.ifBlank { "Driver" }, truck = invite.truckNumber)
                onSuccess()
            } else {
                _state.update { it.copy(isLoading = false, error = "Galat code ya expire ho gaya") }
                onError()
            }
        }
    }

    fun registerUser(onSuccess: () -> Unit) {
        _state.update { it.copy(isLoading = true) }
        // TODO: POST /api/auth/register
        prefs.userRole = _state.value.role.name
        saveAuth(name = _state.value.name, truck = _state.value.truckNumber)
        _state.update { it.copy(isLoading = false) }
        onSuccess()
    }

    fun saveAuthForWhatsappUser() {
        saveAuth(name = _state.value.existingUserName, truck = "MH12AB1234")
    }

    private fun saveAuth(name: String, truck: String) {
        val s = _state.value
        prefs.saveLogin(
            token = "mock_token_${System.currentTimeMillis()}",
            userId = "user_${s.phone}", name = name, truck = truck,
            role = s.role.name, orgId = prefs.organizationId, assignedTruck = prefs.assignedTruckId,
        )
    }

    fun startResendCountdown() { _state.update { it.copy(resendCountdown = 60) } }
    fun tickCountdown() { _state.update { it.copy(resendCountdown = (it.resendCountdown - 1).coerceAtLeast(0)) } }
    fun resendOtp() { startResendCountdown() }

    class Factory(private val prefs: AuthPreferences, private val fleetRepo: FleetRepository? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(prefs, fleetRepo) as T
    }
}
