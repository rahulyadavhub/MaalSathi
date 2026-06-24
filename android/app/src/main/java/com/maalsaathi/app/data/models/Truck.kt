package com.maalsaathi.app.data.models

data class Truck(
    val id: String,
    val organizationId: String,
    val registrationNumber: String,
    val model: String = "",
    val assignedDriverId: String? = null,
    val assignedDriverName: String = "",
    val imageRequired: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
