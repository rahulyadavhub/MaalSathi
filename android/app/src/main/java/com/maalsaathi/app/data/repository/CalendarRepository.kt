package com.maalsaathi.app.data.repository

import com.maalsaathi.app.data.local.CalendarReminderDao
import com.maalsaathi.app.data.local.toDomain
import com.maalsaathi.app.data.local.toEntity
import com.maalsaathi.app.data.models.CalendarReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CalendarRepository(
    private val dao: CalendarReminderDao,
) {
    fun getAll(): Flow<List<CalendarReminder>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    fun getUpcoming(): Flow<List<CalendarReminder>> =
        dao.getUpcoming().map { list -> list.map { it.toDomain() } }

    suspend fun getByDateRange(from: Long, to: Long): List<CalendarReminder> =
        dao.getByDateRange(from, to).map { it.toDomain() }

    suspend fun add(reminder: CalendarReminder) {
        dao.insert(reminder.toEntity())
    }

    suspend fun markDone(id: String) {
        val all = dao.getByDateRange(0, Long.MAX_VALUE)
        val entity = all.find { it.id == id } ?: return
        dao.update(entity.copy(isDone = true))
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }
}
