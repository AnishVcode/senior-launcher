package com.example.senioroslauncher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.senioroslauncher.data.database.AppDatabase

class SeniorLauncherApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Medication Channel - High Priority
            val medicationChannel = NotificationChannel(
                CHANNEL_MEDICATION,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medications"
                enableVibration(true)
                setShowBadge(true)
            }

            // Emergency Channel - High Priority, Bypass DND
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency and SOS alerts"
                enableVibration(true)
                setBypassDnd(true)
                setShowBadge(true)
            }

            // Fall Detection Channel - High Priority, Bypass DND
            val fallDetectionChannel = NotificationChannel(
                CHANNEL_FALL_DETECTION,
                "Fall Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Fall detection alerts"
                enableVibration(true)
                setBypassDnd(true)
                setShowBadge(true)
            }

            // Hydration Channel - Default Priority
            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
                enableVibration(true)
            }

            // Appointments Channel - High Priority
            val appointmentsChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for appointments"
                enableVibration(true)
            }

            // General Channel - Default Priority
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Service Channel - Low Priority (for foreground services)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Background Services",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service notifications"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(
                    medicationChannel,
                    emergencyChannel,
                    fallDetectionChannel,
                    hydrationChannel,
                    appointmentsChannel,
                    generalChannel,
                    serviceChannel
                )
            )
        }
    }

    companion object {
        const val CHANNEL_MEDICATION = "medication_channel"
        const val CHANNEL_EMERGENCY = "emergency_channel"
        const val CHANNEL_FALL_DETECTION = "fall_detection_channel"
        const val CHANNEL_HYDRATION = "hydration_channel"
        const val CHANNEL_APPOINTMENTS = "appointments_channel"
        const val CHANNEL_GENERAL = "general_channel"
        const val CHANNEL_SERVICE = "service_channel"
    }
}
