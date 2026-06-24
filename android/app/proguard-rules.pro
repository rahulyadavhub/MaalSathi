# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.maalsaathi.app.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Gson
-keep class com.google.gson.reflect.TypeToken { *; }
