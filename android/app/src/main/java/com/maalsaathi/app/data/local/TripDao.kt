package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = 'ONGOING' LIMIT 1")
    fun getOngoing(): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE status = 'SCHEDULED' ORDER BY scheduledDate ASC")
    fun getScheduled(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status IN ('COMPLETED','CANCELLED') ORDER BY endTime DESC")
    fun getPast(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity)

    @Update
    suspend fun update(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM trips WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE truckId = :truckId ORDER BY startTime DESC")
    fun getByTruck(truckId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE truckId = :truckId AND status = 'ONGOING' LIMIT 1")
    suspend fun getOngoingByTruck(truckId: String): TripEntity?

    @Query("SELECT * FROM trips WHERE organizationId = :orgId AND status = 'ONGOING'")
    fun getOngoingByOrg(orgId: String): Flow<List<TripEntity>>
}
