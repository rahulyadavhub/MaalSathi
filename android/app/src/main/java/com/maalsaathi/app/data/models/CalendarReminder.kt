package com.maalsaathi.app.data.models

data class CalendarReminder(
    val id: String,
    val type: ReminderType,
    val title: String,
    val amount: Long? = null,
    val partyName: String? = null,
    val dueDate: Long,
    val isDone: Boolean = false,
    val linkedTripId: String? = null,
)
