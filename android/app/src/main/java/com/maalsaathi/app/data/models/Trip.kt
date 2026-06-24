package com.maalsaathi.app.data.models

data class Trip(
    val id: String,
    val userId: String = "",
    val organizationId: String? = null,
    val truckId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val origin: String,
    val destination: String,
    val cargoType: String = "",
    val cargoWeightTons: Double = 0.0,
    val freightAmount: Long = 0,
    val advanceAmount: Long = 0,
    val status: TripStatus,
    val startTime: Long,
    val endTime: Long? = null,
    val scheduledDate: Long? = null,
    val partyName: String = "",
    val entries: List<TripEntry> = emptyList(),
) {
    val totalExpenses: Long get() = entries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
    val totalIncome: Long get() = freightAmount + entries.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
    val netProfit: Long get() = totalIncome - totalExpenses
    val durationMillis: Long? get() = endTime?.let { it - startTime }
}
