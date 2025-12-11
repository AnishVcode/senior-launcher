package com.example.senioroslauncher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "medical_profile")
data class MedicalProfileEntity(
    @PrimaryKey
    val id: Long = 1, // Single profile
    val bloodType: String = "",
    val allergies: String = "",
    val medicalConditions: String = "",
    val emergencyNotes: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val insuranceInfo: String = "",
    val updatedAt: Date = Date()
)
