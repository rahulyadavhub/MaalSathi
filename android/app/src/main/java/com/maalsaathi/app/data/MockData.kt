package com.maalsaathi.app.data

import com.maalsaathi.app.data.local.AppDatabase
import com.maalsaathi.app.data.local.toEntity
import com.maalsaathi.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MockData {

    suspend fun seedIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        if (db.tripDao().getById("mock_ongoing") != null) return@withContext

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        // ── Organization ──
        val org = Organization("org1", "user_rahul", "Sharma Transport", "Rahul Yadav", "9912345678", now - 30 * day)
        db.organizationDao().insert(org.toEntity())

        // ── Trucks ──
        val trucks = listOf(
            Truck("t1", "org1", "MH12AB1234", "Tata 407", assignedDriverId = "d1", assignedDriverName = "Ramesh Kumar", imageRequired = true, createdAt = now - 20 * day),
            Truck("t2", "org1", "MH14CD5678", "Ashok Leyland", assignedDriverId = "d2", assignedDriverName = "Suresh Yadav", imageRequired = false, createdAt = now - 15 * day),
            Truck("t3", "org1", "DL01EF9012", "Mahindra Bolero", createdAt = now - 5 * day),
        )
        trucks.forEach { db.truckDao().insert(it.toEntity()) }

        // ── Pending invite for truck 3 ──
        val invite = DriverInvite("inv1", "org1", "t3", "DL01EF9012", "Rahul Yadav", "MS1234", driverPhone = "7788001122", status = InviteStatus.PENDING, createdAt = now - 1 * day, expiresAt = now + 6 * day)
        db.driverInviteDao().insert(invite.toEntity())

        // ── Ongoing trip on truck 1 ──
        val ongoingId = "mock_ongoing"
        val ongoingStart = now - 6 * hour
        val ongoing = Trip(id = ongoingId, userId = "user_rahul", organizationId = "org1", truckId = "t1", driverId = "d1", driverName = "Ramesh Kumar", origin = "Mumbai", destination = "Delhi", cargoType = "Cement", cargoWeightTons = 50.0, freightAmount = 200_000, advanceAmount = 20_000, status = TripStatus.ONGOING, startTime = ongoingStart)
        db.tripDao().insert(ongoing.toEntity())

        listOf(
            TripEntry("oe1", ongoingId, "t1", "d1", "Ramesh", EntryType.EXPENSE, "diesel", 4500, "⛽", "HP Pump Nashik", null, true, ongoingStart + 1 * hour, "diesel 4500", UserRole.DRIVER),
            TripEntry("oe2", ongoingId, "t1", "d1", "Ramesh", EntryType.EXPENSE, "toll", 350, "🎟️", "NH-44", null, false, ongoingStart + 2 * hour, "toll 350", UserRole.DRIVER),
            TripEntry("oe3", ongoingId, "t1", "d1", "Ramesh", EntryType.EXPENSE, "food", 250, "🍽️", "Dhaba lunch", null, false, ongoingStart + 3 * hour, "khana 250", UserRole.DRIVER),
            TripEntry("oe4", ongoingId, "t1", "d1", "Ramesh", EntryType.EXPENSE, "toll", 600, "🎟️", "Highway toll", null, false, ongoingStart + 4 * hour, "toll 600", UserRole.DRIVER),
            TripEntry("oe5", ongoingId, "t1", "d1", "Ramesh", EntryType.EXPENSE, "diesel", 2500, "⛽", "IOC Pump", null, true, ongoingStart + 5 * hour, "diesel 2500", UserRole.DRIVER),
        ).forEach { db.tripEntryDao().insert(it.toEntity()) }

        // ── Ongoing trip on truck 2 ──
        val trip2Id = "mock_ongoing2"
        val trip2Start = now - 3 * hour
        val trip2 = Trip(id = trip2Id, userId = "user_rahul", organizationId = "org1", truckId = "t2", driverId = "d2", driverName = "Suresh Yadav", origin = "Pune", destination = "Bangalore", cargoType = "Electronics", cargoWeightTons = 10.0, freightAmount = 120_000, status = TripStatus.ONGOING, startTime = trip2Start)
        db.tripDao().insert(trip2.toEntity())

        listOf(
            TripEntry("t2e1", trip2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "diesel", 3500, "⛽", "", null, false, trip2Start + 1 * hour, "", UserRole.DRIVER),
            TripEntry("t2e2", trip2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "toll", 200, "🎟️", "", null, false, trip2Start + 2 * hour, "", UserRole.DRIVER),
        ).forEach { db.tripEntryDao().insert(it.toEntity()) }

        // ── Scheduled trip ──
        val scheduledId = "mock_scheduled"
        val tomorrow = now + day
        db.tripDao().insert(Trip(id = scheduledId, userId = "user_rahul", organizationId = "org1", truckId = "t1", origin = "Delhi", destination = "Pune", partyName = "Ramesh Transport", status = TripStatus.SCHEDULED, startTime = tomorrow, scheduledDate = tomorrow).toEntity())

        // ── Past trips ──
        val past1Id = "mock_past1"
        val past1Start = now - 3 * day
        db.tripDao().insert(Trip(id = past1Id, userId = "user_rahul", organizationId = "org1", truckId = "t1", driverId = "d1", driverName = "Ramesh Kumar", origin = "Pune", destination = "Chennai", cargoType = "Steel", cargoWeightTons = 20.0, freightAmount = 150_000, status = TripStatus.COMPLETED, startTime = past1Start, endTime = past1Start + 14 * hour).toEntity())
        listOf(
            TripEntry("p1e1", past1Id, "t1", "d1", "Ramesh", EntryType.EXPENSE, "diesel", 18_000, "⛽", "", null, true, past1Start + 2 * hour, "", UserRole.DRIVER),
            TripEntry("p1e2", past1Id, "t1", "d1", "Ramesh", EntryType.EXPENSE, "toll", 2_100, "🎟️", "", null, false, past1Start + 4 * hour, "", UserRole.DRIVER),
            TripEntry("p1e3", past1Id, "t1", "d1", "Ramesh", EntryType.EXPENSE, "food", 800, "🍽️", "", null, false, past1Start + 7 * hour, "", UserRole.DRIVER),
            TripEntry("p1e4", past1Id, "t1", "d1", "Ramesh", EntryType.EXPENSE, "repair", 500, "🔧", "Puncture", null, false, past1Start + 9 * hour, "", UserRole.DRIVER),
        ).forEach { db.tripEntryDao().insert(it.toEntity()) }

        val past2Id = "mock_past2"
        val past2Start = now - 5 * day
        db.tripDao().insert(Trip(id = past2Id, userId = "user_rahul", organizationId = "org1", truckId = "t2", driverId = "d2", driverName = "Suresh Yadav", origin = "Mumbai", destination = "Bangalore", cargoType = "Electronics", cargoWeightTons = 10.0, freightAmount = 120_000, status = TripStatus.COMPLETED, startTime = past2Start, endTime = past2Start + 18 * hour).toEntity())
        listOf(
            TripEntry("p2e1", past2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "diesel", 15_000, "⛽", "", null, false, past2Start + 3 * hour, "", UserRole.DRIVER),
            TripEntry("p2e2", past2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "toll", 4_500, "🎟️", "", null, false, past2Start + 5 * hour, "", UserRole.DRIVER),
            TripEntry("p2e3", past2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "food", 1_200, "🍽️", "", null, false, past2Start + 8 * hour, "", UserRole.DRIVER),
            TripEntry("p2e4", past2Id, "t2", "d2", "Suresh", EntryType.EXPENSE, "tyre", 8_000, "🛞", "Front tyre", null, false, past2Start + 12 * hour, "", UserRole.DRIVER),
        ).forEach { db.tripEntryDao().insert(it.toEntity()) }

        // ── Calendar reminders ──
        db.calendarReminderDao().insert(CalendarReminder("rem1", ReminderType.ADVANCE_LENA, "Advance Lena", 20_000, "Ramesh Transport", now + day).toEntity())
        db.calendarReminderDao().insert(CalendarReminder("rem2", ReminderType.UDHARI_CHUKANI, "Udhari Chukani", 5_000, "Sharma Ji", now - 2 * day).toEntity())
        db.calendarReminderDao().insert(CalendarReminder("rem3", ReminderType.CUSTOM, "Insurance Renewal", 15_000, null, now + 5 * day).toEntity())
    }
}
