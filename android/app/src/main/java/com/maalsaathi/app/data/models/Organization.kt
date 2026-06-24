package com.maalsaathi.app.data.models

data class Organization(
    val id: String,
    val ownerId: String,
    val name: String,
    val ownerName: String,
    val ownerPhone: String,
    val createdAt: Long = System.currentTimeMillis(),
)
