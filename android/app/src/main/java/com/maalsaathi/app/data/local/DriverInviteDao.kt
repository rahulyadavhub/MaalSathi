package com.maalsaathi.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverInviteDao {
    @Query("SELECT * FROM driver_invites WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun getByOrganization(orgId: String): Flow<List<DriverInviteEntity>>

    @Query("SELECT * FROM driver_invites WHERE inviteCode = :code LIMIT 1")
    suspend fun getByCode(code: String): DriverInviteEntity?

    @Query("SELECT * FROM driver_invites WHERE organizationId = :orgId AND status = 'PENDING'")
    fun getPending(orgId: String): Flow<List<DriverInviteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invite: DriverInviteEntity)

    @Update
    suspend fun update(invite: DriverInviteEntity)
}
