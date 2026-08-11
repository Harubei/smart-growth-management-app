package com.smartgrowth.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutors")
data class Tutor(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val maxCapacity: Int,
    val hireDate: String = "",
    val availability: String = "",
    val subjectsHandled: String = "",
    val isSynced: Boolean = false
)