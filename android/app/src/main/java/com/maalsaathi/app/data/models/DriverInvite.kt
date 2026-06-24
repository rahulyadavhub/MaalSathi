package com.maalsaathi.app.data.models

data class DriverInvite(
    val id: String,
    val organizationId: String,
    val truckId: String,
    val truckNumber: String,
    val ownerName: String,
    val inviteCode: String,
    val inviteLink: String = "maalsaathi://invite/$inviteCode",
    val driverPhone: String? = null,
    val driverName: String? = null,
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = createdAt + 7 * 86_400_000L,
)
