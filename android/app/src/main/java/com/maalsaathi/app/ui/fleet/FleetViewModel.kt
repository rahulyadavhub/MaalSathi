package com.maalsaathi.app.ui.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maalsaathi.app.data.models.DriverInvite
import com.maalsaathi.app.data.models.InviteStatus
import com.maalsaathi.app.data.models.Truck
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.data.repository.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FleetState(
    val orgName: String = "",
    val ownerName: String = "",
    val todayEarnings: Long = 0,
    val monthEarnings: Long = 0,
    val recentActivity: List<TripEntry> = emptyList(),
)

class FleetViewModel(
    private val repo: FleetRepository,
    private val orgId: String,
) : ViewModel() {

    val trucks = repo.getTrucks(orgId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingInvites = repo.getPendingInvites(orgId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(FleetState())
    val state: StateFlow<FleetState> = _state

    init {
        loadFleetData()
    }

    private fun loadFleetData() {
        viewModelScope.launch {
            val org = repo.getOrganization(orgId)
            val recent = repo.getRecentEntries(10)
            _state.update {
                it.copy(
                    orgName = org?.name ?: "",
                    ownerName = org?.ownerName ?: "",
                    todayEarnings = 45_000,  // TODO: compute from today's completed trips
                    monthEarnings = 340_000, // TODO: compute from this month's trips
                    recentActivity = recent,
                )
            }
        }
    }

    fun addTruck(registrationNumber: String, model: String = "", imageRequired: Boolean = false) {
        viewModelScope.launch {
            val truck = Truck(
                id = UUID.randomUUID().toString(),
                organizationId = orgId,
                registrationNumber = registrationNumber.uppercase(),
                model = model,
                imageRequired = imageRequired,
            )
            repo.addTruck(truck)
        }
    }

    fun setImageRequired(truckId: String, required: Boolean) {
        viewModelScope.launch { repo.setImageRequired(truckId, required) }
    }

    fun removeDriver(truckId: String) {
        viewModelScope.launch { repo.removeDriver(truckId) }
    }

    fun generateInvite(truckId: String, truckNumber: String, ownerName: String, driverPhone: String? = null): DriverInvite {
        val code = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
        val invite = DriverInvite(
            id = UUID.randomUUID().toString(),
            organizationId = orgId,
            truckId = truckId,
            truckNumber = truckNumber,
            ownerName = ownerName,
            inviteCode = code,
            driverPhone = driverPhone,
        )
        viewModelScope.launch { repo.createInvite(invite) }
        return invite
    }

    class Factory(private val repo: FleetRepository, private val orgId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FleetViewModel(repo, orgId) as T
    }
}
