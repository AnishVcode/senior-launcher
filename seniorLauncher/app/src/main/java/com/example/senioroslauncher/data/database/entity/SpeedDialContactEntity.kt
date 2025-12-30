package com.example.senioroslauncher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_dial_contacts")
data class SpeedDialContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val position: Int // 0-4 for 5 speed dial slots
)
