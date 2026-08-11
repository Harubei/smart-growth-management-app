package com.smartgrowth.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val studentName: String,
    val tutorName: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val program: String,
    val status: String = "Scheduled",
    val isSynced: Boolean = false
)