package com.maalsaathi.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maalsaathi.app.data.models.CalendarReminder
import com.maalsaathi.app.data.models.ReminderType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.data.repository.CalendarRepository
import com.maalsaathi.app.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class CalendarState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val reminders: List<CalendarReminder> = emptyList(),
    val scheduledTrips: List<Trip> = emptyList(),
    val selectedDateReminders: List<CalendarReminder> = emptyList(),
    val selectedDateTrips: List<Trip> = emptyList(),
    val selectedDay: Int? = null,
)

class CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val tripRepo: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state

    init {
        viewModelScope.launch {
            combine(calendarRepo.getAll(), tripRepo.getScheduledTrips()) { reminders, trips ->
                reminders to trips
            }.collect { (reminders, trips) ->
                _state.update { it.copy(reminders = reminders, scheduledTrips = trips) }
            }
        }
    }

    val upcomingReminders = calendarRepo.getUpcoming()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: Int) {
        val s = _state.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, s.selectedYear)
            set(Calendar.MONTH, s.selectedMonth)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val dayEnd = cal.timeInMillis

        val dayReminders = s.reminders.filter { it.dueDate in dayStart until dayEnd }
        val dayTrips = s.scheduledTrips.filter { (it.scheduledDate ?: it.startTime) in dayStart until dayEnd }
        _state.update { it.copy(selectedDay = day, selectedDateReminders = dayReminders, selectedDateTrips = dayTrips) }
    }

    fun changeMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _state.value.selectedYear)
            set(Calendar.MONTH, _state.value.selectedMonth)
            add(Calendar.MONTH, delta)
        }
        _state.update { it.copy(selectedYear = cal.get(Calendar.YEAR), selectedMonth = cal.get(Calendar.MONTH), selectedDay = null) }
    }

    fun addReminder(type: ReminderType, title: String, amount: Long?, partyName: String?, dueDate: Long) {
        viewModelScope.launch {
            calendarRepo.add(CalendarReminder(
                id = UUID.randomUUID().toString(), type = type, title = title,
                amount = amount, partyName = partyName, dueDate = dueDate,
            ))
        }
    }

    fun scheduleTrip(origin: String, destination: String, partyName: String, date: Long) {
        viewModelScope.launch {
            tripRepo.createTrip(Trip(
                id = UUID.randomUUID().toString(), origin = origin, destination = destination,
                partyName = partyName, status = TripStatus.SCHEDULED,
                startTime = date, scheduledDate = date,
            ))
        }
    }

    fun markReminderDone(id: String) { viewModelScope.launch { calendarRepo.markDone(id) } }
    fun deleteReminder(id: String) { viewModelScope.launch { calendarRepo.delete(id) } }

    class Factory(private val calendarRepo: CalendarRepository, private val tripRepo: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(calendarRepo, tripRepo) as T
    }
}
