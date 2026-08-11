package com.smartgrowth.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val studentName: String,
    val amount: Double,
    val date: String,
    val method: String,
    val notes: String,
    val isSynced: Boolean = false
)