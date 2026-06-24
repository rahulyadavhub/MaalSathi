package com.maalsaathi.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TripEntity::class,
        TripEntryEntity::class,
        CalendarReminderEntity::class,
        OrganizationEntity::class,
        TruckEntity::class,
        DriverInviteEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun tripEntryDao(): TripEntryDao
    abstract fun calendarReminderDao(): CalendarReminderDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun truckDao(): TruckDao
    abstract fun driverInviteDao(): DriverInviteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "maalsaathi.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
