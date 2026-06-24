package com.maalsaathi.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maalsaathi.app.data.models.*

// ─── Organization ────────────────────────────────

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val ownerName: String,
    val ownerPhone: String,
    val createdAt: Long,
)

fun OrganizationEntity.toDomain() = Organization(id = id, ownerId = ownerId, name = name, ownerName = ownerName, ownerPhone = ownerPhone, createdAt = createdAt)
fun Organization.toEntity() = OrganizationEntity(id = id, ownerId = ownerId, name = name, ownerName = ownerName, ownerPhone = ownerPhone, createdAt = createdAt)

// ─── Truck ───────────────────────────────────────

@Entity(tableName = "trucks", indices = [Index("organizationId")])
data class TruckEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val registrationNumber: String,
    val model: String = "",
    val assignedDriverId: String? = null,
    val assignedDriverName: String = "",
    val imageRequired: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long,
)

fun TruckEntity.toDomain() = Truck(id = id, organizationId = organizationId, registrationNumber = registrationNumber, model = model, assignedDriverId = assignedDriverId, assignedDriverName = assignedDriverName, imageRequired = imageRequired, isActive = isActive, createdAt = createdAt)
fun Truck.toEntity() = TruckEntity(id = id, organizationId = organizationId, registrationNumber = registrationNumber, model = model, assignedDriverId = assignedDriverId, assignedDriverName = assignedDriverName, imageRequired = imageRequired, isActive = isActive, createdAt = createdAt)

// ─── Driver Invite ───────────────────────────────

@Entity(tableName = "driver_invites", indices = [Index("inviteCode", unique = true)])
data class DriverInviteEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val truckId: String,
    val truckNumber: String,
    val ownerName: String,
    val inviteCode: String,
    val inviteLink: String,
    val driverPhone: String? = null,
    val driverName: String? = null,
    val status: InviteStatus,
    val createdAt: Long,
    val expiresAt: Long,
)

fun DriverInviteEntity.toDomain() = DriverInvite(id = id, organizationId = organizationId, truckId = truckId, truckNumber = truckNumber, ownerName = ownerName, inviteCode = inviteCode, inviteLink = inviteLink, driverPhone = driverPhone, driverName = driverName, status = status, createdAt = createdAt, expiresAt = expiresAt)
fun DriverInvite.toEntity() = DriverInviteEntity(id = id, organizationId = organizationId, truckId = truckId, truckNumber = truckNumber, ownerName = ownerName, inviteCode = inviteCode, inviteLink = inviteLink, driverPhone = driverPhone, driverName = driverName, status = status, createdAt = createdAt, expiresAt = expiresAt)

// ─── Trip (updated) ──────────────────────────────

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val userId: String = "",
    val organizationId: String? = null,
    val truckId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val origin: String,
    val destination: String,
    val cargoType: String = "",
    val cargoWeightTons: Double = 0.0,
    val freightAmount: Long = 0,
    val advanceAmount: Long = 0,
    val status: TripStatus,
    val startTime: Long,
    val endTime: Long? = null,
    val scheduledDate: Long? = null,
    val partyName: String = "",
    val pendingSync: Boolean = false,
)

fun TripEntity.toDomain(entries: List<TripEntry> = emptyList()) = Trip(
    id = id, userId = userId, organizationId = organizationId, truckId = truckId,
    driverId = driverId, driverName = driverName, origin = origin, destination = destination,
    cargoType = cargoType, cargoWeightTons = cargoWeightTons,
    freightAmount = freightAmount, advanceAmount = advanceAmount,
    status = status, startTime = startTime, endTime = endTime,
    scheduledDate = scheduledDate, partyName = partyName, entries = entries,
)

fun Trip.toEntity(pendingSync: Boolean = false) = TripEntity(
    id = id, userId = userId, organizationId = organizationId, truckId = truckId,
    driverId = driverId, driverName = driverName, origin = origin, destination = destination,
    cargoType = cargoType, cargoWeightTons = cargoWeightTons,
    freightAmount = freightAmount, advanceAmount = advanceAmount,
    status = status, startTime = startTime, endTime = endTime,
    scheduledDate = scheduledDate, partyName = partyName, pendingSync = pendingSync,
)

// ─── TripEntry (updated) ─────────────────────────

@Entity(
    tableName = "trip_entries",
    foreignKeys = [ForeignKey(entity = TripEntity::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tripId"), Index("truckId")],
)
data class TripEntryEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val truckId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val type: EntryType,
    val category: String,
    val amount: Long,
    val emoji: String,
    val note: String = "",
    val imageUrl: String? = null,
    val imageRequired: Boolean = false,
    val timestamp: Long,
    val rawText: String = "",
    val createdByRole: UserRole = UserRole.MAALIK_DRIVER,
    val pendingSync: Boolean = false,
)

fun TripEntryEntity.toDomain() = TripEntry(
    id = id, tripId = tripId, truckId = truckId, driverId = driverId, driverName = driverName,
    type = type, category = category, amount = amount, emoji = emoji, note = note,
    imageUrl = imageUrl, imageRequired = imageRequired, timestamp = timestamp,
    rawText = rawText, createdByRole = createdByRole,
)

fun TripEntry.toEntity(pendingSync: Boolean = false) = TripEntryEntity(
    id = id, tripId = tripId, truckId = truckId, driverId = driverId, driverName = driverName,
    type = type, category = category, amount = amount, emoji = emoji, note = note,
    imageUrl = imageUrl, imageRequired = imageRequired, timestamp = timestamp,
    rawText = rawText, createdByRole = createdByRole, pendingSync = pendingSync,
)

// ─── CalendarReminder (updated) ──────────────────

@Entity(tableName = "calendar_reminders")
data class CalendarReminderEntity(
    @PrimaryKey val id: String,
    val organizationId: String? = null,
    val userId: String = "",
    val type: ReminderType,
    val title: String,
    val amount: Long? = null,
    val partyName: String? = null,
    val dueDate: Long,
    val isDone: Boolean = false,
    val linkedTripId: String? = null,
)

fun CalendarReminderEntity.toDomain() = CalendarReminder(
    id = id, type = type, title = title, amount = amount,
    partyName = partyName, dueDate = dueDate, isDone = isDone, linkedTripId = linkedTripId,
)

fun CalendarReminder.toEntity() = CalendarReminderEntity(
    id = id, type = type, title = title, amount = amount,
    partyName = partyName, dueDate = dueDate, isDone = isDone, linkedTripId = linkedTripId,
)
