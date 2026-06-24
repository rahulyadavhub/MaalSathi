package com.maalsaathi.app.data.repository

import com.maalsaathi.app.data.local.DriverInviteDao
import com.maalsaathi.app.data.local.OrganizationDao
import com.maalsaathi.app.data.local.TripDao
import com.maalsaathi.app.data.local.TruckDao
import com.maalsaathi.app.data.local.TripEntryDao
import com.maalsaathi.app.data.local.toDomain
import com.maalsaathi.app.data.local.toEntity
import com.maalsaathi.app.data.models.DriverInvite
import com.maalsaathi.app.data.models.InviteStatus
import com.maalsaathi.app.data.models.Organization
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.Truck
import com.maalsaathi.app.data.models.TripEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FleetRepository(
    private val orgDao: OrganizationDao,
    private val truckDao: TruckDao,
    private val inviteDao: DriverInviteDao,
    private val entryDao: TripEntryDao,
    private val tripDao: TripDao? = null,
) {
    fun getTrucks(orgId: String): Flow<List<Truck>> =
        truckDao.getByOrganization(orgId).map { list -> list.map { it.toDomain() } }

    suspend fun getTruckById(id: String): Truck? =
        truckDao.getById(id)?.toDomain()

    suspend fun addTruck(truck: Truck) {
        truckDao.insert(truck.toEntity())
    }

    suspend fun setImageRequired(truckId: String, required: Boolean) {
        truckDao.setImageRequired(truckId, required)
    }

    suspend fun assignDriver(truckId: String, driverId: String, driverName: String) {
        truckDao.assignDriver(truckId, driverId, driverName)
    }

    suspend fun removeDriver(truckId: String) {
        truckDao.removeDriver(truckId)
    }

    suspend fun createOrganization(org: Organization) {
        orgDao.insert(org.toEntity())
    }

    suspend fun getOrganization(orgId: String): Organization? =
        orgDao.getById(orgId)?.toDomain()

    suspend fun createInvite(invite: DriverInvite) {
        inviteDao.insert(invite.toEntity())
    }

    suspend fun getInviteByCode(code: String): DriverInvite? =
        inviteDao.getByCode(code)?.toDomain()

    suspend fun acceptInvite(code: String, driverId: String, driverName: String) {
        val entity = inviteDao.getByCode(code) ?: return
        inviteDao.update(entity.copy(status = InviteStatus.ACCEPTED, driverName = driverName))
        truckDao.assignDriver(entity.truckId, driverId, driverName)
    }

    fun getPendingInvites(orgId: String): Flow<List<DriverInvite>> =
        inviteDao.getPending(orgId).map { list -> list.map { it.toDomain() } }

    suspend fun getRecentEntries(limit: Int = 20): List<TripEntry> =
        entryDao.getByDateRange(0, Long.MAX_VALUE).map { it.toDomain() }
            .sortedByDescending { it.timestamp }.take(limit)

    fun getOngoingTripsByOrg(orgId: String): Flow<List<Trip>> =
        tripDao?.getOngoingByOrg(orgId)?.map { list ->
            list.map { entity ->
                val entries = entryDao.getByTripIdOnce(entity.id).map { it.toDomain() }
                entity.toDomain(entries)
            }
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getOngoingTripForTruck(truckId: String): Trip? {
        val entity = tripDao?.getOngoingByTruck(truckId) ?: return null
        val entries = entryDao.getByTripIdOnce(entity.id).map { it.toDomain() }
        return entity.toDomain(entries)
    }
}
