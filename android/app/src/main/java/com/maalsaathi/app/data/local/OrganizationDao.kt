package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations WHERE id = :id")
    suspend fun getById(id: String): OrganizationEntity?

    @Query("SELECT * FROM organizations WHERE ownerId = :ownerId LIMIT 1")
    suspend fun getByOwner(ownerId: String): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: OrganizationEntity)
}
