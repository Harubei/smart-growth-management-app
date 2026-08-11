package com.smartgrowth.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutors")
data class Tutor(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val fullName: String,
    val phone: String,
    val maxCapacity: Int,
    val availability: String = "", // Format: "Mon|08:00 AM|05:00 PM;Tue|10:00 AM|02:00 PM"
    val isSynced: Boolean = false
)