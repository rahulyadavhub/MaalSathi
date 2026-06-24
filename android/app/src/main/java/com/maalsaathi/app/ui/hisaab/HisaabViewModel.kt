package com.maalsaathi.app.ui.hisaab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class HisaabPeriod(val label: String) { WEEK("Is Hafte"), MONTH("Is Mahine") }

data class CategoryStat(val emoji: String, val category: String, val total: Long, val percent: Float)

data class HisaabState(
    val period: HisaabPeriod = HisaabPeriod.WEEK,
    val trips: List<Trip> = emptyList(),
    val totalBhada: Long = 0,
    val totalKharcha: Long = 0,
    val netFayda: Long = 0,
    val categoryBreakdown: List<CategoryStat> = emptyList(),
)

class HisaabViewModel(private val repo: TripRepository) : ViewModel() {
    private val _state = MutableStateFlow(HisaabState())
    val state: StateFlow<HisaabState> = _state

    init { loadData() }

    fun setPeriod(p: HisaabPeriod) {
        _state.update { it.copy(period = p) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repo.getAllTrips().collect { allTrips ->
                val (from, to) = periodRange(_state.value.period)
                val filtered = allTrips.filter { t ->
                    (t.status == TripStatus.COMPLETED || t.status == TripStatus.ONGOING) &&
                    t.startTime in from..to
                }
                val totalBhada = filtered.sumOf { it.freightAmount }
                val totalKharcha = filtered.sumOf { it.totalExpenses }
                val allExpenses = filtered.flatMap { it.entries.filter { e -> e.type == EntryType.EXPENSE } }
                val byCategory = allExpenses.groupBy { it.category }
                val total = allExpenses.sumOf { it.amount }.coerceAtLeast(1)
                val breakdown = byCategory.map { (cat, entries) ->
                    val sum = entries.sumOf { it.amount }
                    CategoryStat(entries.first().emoji, cat, sum, sum.toFloat() / total)
                }.sortedByDescending { it.total }

                _state.update {
                    it.copy(trips = filtered, totalBhada = totalBhada, totalKharcha = totalKharcha,
                        netFayda = totalBhada - totalKharcha, categoryBreakdown = breakdown)
                }
            }
        }
    }

    private fun periodRange(p: HisaabPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val to = cal.timeInMillis
        when (p) {
            HisaabPeriod.WEEK -> {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val diff = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_MONTH, -diff)
            }
            HisaabPeriod.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis to to
    }

    class Factory(private val repo: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HisaabViewModel(repo) as T
    }
}
