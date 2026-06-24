package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TruckDao {
    @Query("SELECT * FROM trucks WHERE organizationId = :orgId AND isActive = 1 ORDER BY createdAt ASC")
    fun getByOrganization(orgId: String): Flow<List<TruckEntity>>

    @Query("SELECT * FROM trucks WHERE id = :id")
    suspend fun getById(id: String): TruckEntity?

    @Query("SELECT * FROM trucks WHERE assignedDriverId = :driverId LIMIT 1")
    suspend fun getByDriver(driverId: String): TruckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(truck: TruckEntity)

    @Update
    suspend fun update(truck: TruckEntity)

    @Query("UPDATE trucks SET assignedDriverId = :driverId, assignedDriverName = :driverName WHERE id = :truckId")
    suspend fun assignDriver(truckId: String, driverId: String, driverName: String)

    @Query("UPDATE trucks SET assignedDriverId = NULL, assignedDriverName = '' WHERE id = :truckId")
    suspend fun removeDriver(truckId: String)

    @Query("UPDATE trucks SET imageRequired = :required WHERE id = :truckId")
    suspend fun setImageRequired(truckId: String, required: Boolean)
}
