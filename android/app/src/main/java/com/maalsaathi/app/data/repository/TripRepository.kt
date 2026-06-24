package com.maalsaathi.app.data.repository

import com.maalsaathi.app.data.local.TripDao
import com.maalsaathi.app.data.local.TripEntryDao
import com.maalsaathi.app.data.local.toDomain
import com.maalsaathi.app.data.local.toEntity
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.Trip
import com.maalsaathi.app.data.models.TripEntry
import com.maalsaathi.app.data.models.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class TripRepository(
    private val tripDao: TripDao,
    private val entryDao: TripEntryDao,
) {
    fun getOngoingTrip(): Flow<Trip?> =
        tripDao.getOngoing().map { entity ->
            entity?.let {
                val entries = entryDao.getByTripIdOnce(it.id).map { e -> e.toDomain() }
                it.toDomain(entries)
            }
        }

    fun getScheduledTrips(): Flow<List<Trip>> =
        tripDao.getScheduled().map { list ->
            list.map { it.toDomain() }
        }

    fun getPastTrips(): Flow<List<Trip>> =
        tripDao.getPast().map { list ->
            list.map { entity ->
                val entries = entryDao.getByTripIdOnce(entity.id).map { it.toDomain() }
                entity.toDomain(entries)
            }
        }

    fun getAllTrips(): Flow<List<Trip>> =
        tripDao.getAll().map { list ->
            list.map { entity ->
                val entries = entryDao.getByTripIdOnce(entity.id).map { it.toDomain() }
                entity.toDomain(entries)
            }
        }

    fun getEntriesForTrip(tripId: String): Flow<List<TripEntry>> =
        entryDao.getByTripId(tripId).map { list -> list.map { it.toDomain() } }

    suspend fun getTripById(id: String): Trip? {
        val entity = tripDao.getById(id) ?: return null
        val entries = entryDao.getByTripIdOnce(id).map { it.toDomain() }
        return entity.toDomain(entries)
    }

    suspend fun createTrip(trip: Trip): Trip {
        tripDao.insert(trip.toEntity(pendingSync = true))
        if (trip.advanceAmount > 0) {
            val advanceEntry = TripEntry(
                id = UUID.randomUUID().toString(),
                tripId = trip.id,
                type = EntryType.INCOME,
                category = "advance",
                amount = trip.advanceAmount,
                emoji = "💰",
                note = "Advance received",
                timestamp = trip.startTime,
            )
            entryDao.insert(advanceEntry.toEntity(pendingSync = true))
        }
        // TODO: Sync to API in background
        return trip
    }

    suspend fun endTrip(tripId: String) {
        val entity = tripDao.getById(tripId) ?: return
        tripDao.update(entity.copy(status = TripStatus.COMPLETED, endTime = System.currentTimeMillis(), pendingSync = true))
        // TODO: Sync to API
    }

    suspend fun cancelTrip(tripId: String) {
        val entity = tripDao.getById(tripId) ?: return
        tripDao.update(entity.copy(status = TripStatus.CANCELLED, endTime = System.currentTimeMillis(), pendingSync = true))
    }

    suspend fun addEntry(entry: TripEntry) {
        entryDao.insert(entry.toEntity(pendingSync = true))
        // TODO: Sync to API
    }

    suspend fun updateEntry(entry: TripEntry) {
        entryDao.update(entry.toEntity(pendingSync = true))
    }

    suspend fun deleteEntry(entryId: String) {
        entryDao.delete(entryId)
    }

    suspend fun getEntriesByDateRange(from: Long, to: Long): List<TripEntry> =
        entryDao.getByDateRange(from, to).map { it.toDomain() }

    suspend fun startScheduledTrip(tripId: String): Trip? {
        val entity = tripDao.getById(tripId) ?: return null
        val updated = entity.copy(status = TripStatus.ONGOING, startTime = System.currentTimeMillis(), pendingSync = true)
        tripDao.update(updated)
        return getTripById(tripId)
    }
}
