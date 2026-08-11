package com.smartgrowth.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val gradeLevel: String,
    val parentContact: String,
    val parentEmail: String?,
    val enrollmentDate: String,
    val isSynced: Boolean = false
)