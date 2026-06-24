package com.maalsaathi.app.data.local

import androidx.room.TypeConverter
import com.maalsaathi.app.data.models.EntryType
import com.maalsaathi.app.data.models.InviteStatus
import com.maalsaathi.app.data.models.ReminderType
import com.maalsaathi.app.data.models.TripStatus
import com.maalsaathi.app.data.models.UserRole

class Converters {
    @TypeConverter fun fromTripStatus(v: TripStatus): String = v.name
    @TypeConverter fun toTripStatus(v: String): TripStatus = TripStatus.valueOf(v)
    @TypeConverter fun fromEntryType(v: EntryType): String = v.name
    @TypeConverter fun toEntryType(v: String): EntryType = EntryType.valueOf(v)
    @TypeConverter fun fromReminderType(v: ReminderType): String = v.name
    @TypeConverter fun toReminderType(v: String): ReminderType = ReminderType.valueOf(v)
    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = UserRole.valueOf(v)
    @TypeConverter fun fromInviteStatus(v: InviteStatus): String = v.name
    @TypeConverter fun toInviteStatus(v: String): InviteStatus = InviteStatus.valueOf(v)
}
