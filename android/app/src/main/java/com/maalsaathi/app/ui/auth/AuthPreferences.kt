package com.maalsaathi.app.ui.auth

import android.content.Context
import android.content.SharedPreferences
import com.maalsaathi.app.data.models.UserRole

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("maalsaathi_auth", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(v) = prefs.edit().putBoolean("is_logged_in", v).apply()

    var authToken: String
        get() = prefs.getString("auth_token", "") ?: ""
        set(v) = prefs.edit().putString("auth_token", v).apply()

    var userId: String
        get() = prefs.getString("user_id", "") ?: ""
        set(v) = prefs.edit().putString("user_id", v).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(v) = prefs.edit().putString("user_name", v).apply()

    var truckNumber: String
        get() = prefs.getString("truck_number", "") ?: ""
        set(v) = prefs.edit().putString("truck_number", v).apply()

    var userRole: String
        get() = prefs.getString("user_role", "MAALIK_DRIVER") ?: "MAALIK_DRIVER"
        set(v) = prefs.edit().putString("user_role", v).apply()

    val userRoleEnum: UserRole
        get() = try { UserRole.valueOf(userRole) } catch (_: Exception) { UserRole.MAALIK_DRIVER }

    var organizationId: String
        get() = prefs.getString("organization_id", "") ?: ""
        set(v) = prefs.edit().putString("organization_id", v).apply()

    var assignedTruckId: String
        get() = prefs.getString("assigned_truck_id", "") ?: ""
        set(v) = prefs.edit().putString("assigned_truck_id", v).apply()

    var ownerPhone: String
        get() = prefs.getString("owner_phone", "") ?: ""
        set(v) = prefs.edit().putString("owner_phone", v).apply()

    fun saveLogin(token: String, userId: String, name: String, truck: String, role: String, orgId: String = "", assignedTruck: String = "") {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("auth_token", token)
            .putString("user_id", userId)
            .putString("user_name", name)
            .putString("truck_number", truck)
            .putString("user_role", role)
            .putString("organization_id", orgId)
            .putString("assigned_truck_id", assignedTruck)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
