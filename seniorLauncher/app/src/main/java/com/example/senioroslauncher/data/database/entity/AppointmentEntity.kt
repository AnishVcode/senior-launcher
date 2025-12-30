package com.example.senioroslauncher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateTime: Date,
    val location: String = "",
    val description: String = "",
    val reminderMinutesBefore: Int = 30,
    val isReminderEnabled: Boolean = true,
    val createdAt: Date = Date()
)
