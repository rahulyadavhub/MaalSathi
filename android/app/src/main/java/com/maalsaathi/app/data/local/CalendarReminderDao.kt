package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarReminderDao {
    @Query("SELECT * FROM calendar_reminders ORDER BY dueDate ASC")
    fun getAll(): Flow<List<CalendarReminderEntity>>

    @Query("SELECT * FROM calendar_reminders WHERE isDone = 0 ORDER BY dueDate ASC")
    fun getUpcoming(): Flow<List<CalendarReminderEntity>>

    @Query("SELECT * FROM calendar_reminders WHERE dueDate BETWEEN :from AND :to ORDER BY dueDate ASC")
    suspend fun getByDateRange(from: Long, to: Long): List<CalendarReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: CalendarReminderEntity)

    @Update
    suspend fun update(reminder: CalendarReminderEntity)

    @Query("DELETE FROM calendar_reminders WHERE id = :id")
    suspend fun delete(id: String)
}
