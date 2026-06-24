package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripEntryDao {
    @Query("SELECT * FROM trip_entries WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getByTripId(tripId: String): Flow<List<TripEntryEntity>>

    @Query("SELECT * FROM trip_entries WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getByTripIdOnce(tripId: String): List<TripEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TripEntryEntity)

    @Update
    suspend fun update(entry: TripEntryEntity)

    @Query("DELETE FROM trip_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM trip_entries WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    suspend fun getByDateRange(from: Long, to: Long): List<TripEntryEntity>
}
