package com.maalsaathi.app.data.models

data class TripEntry(
    val id: String,
    val tripId: String,
    val truckId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val type: EntryType,
    val category: String,
    val amount: Long,
    val emoji: String,
    val note: String = "",
    val imageUrl: String? = null,
    val imageRequired: Boolean = false,
    val timestamp: Long,
    val rawText: String = "",
    val createdByRole: UserRole = UserRole.MAALIK_DRIVER,
)
