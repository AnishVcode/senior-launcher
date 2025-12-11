package com.example.senioroslauncher.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.senioroslauncher.MainActivity
import com.example.senioroslauncher.R
import com.example.senioroslauncher.SeniorLauncherApp

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            ACTION_HYDRATION_REMINDER -> {
                showHydrationReminder(context)
            }
            ACTION_APPOINTMENT_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_APPOINTMENT_TITLE) ?: "Appointment"
                val time = intent.getStringExtra(EXTRA_APPOINTMENT_TIME) ?: ""
                showAppointmentReminder(context, title, time)
            }
        }
    }

    private fun showHydrationReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SeniorLauncherApp.CHANNEL_HYDRATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to drink water!")
            .setContentText("Stay hydrated for better health")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(HYDRATION_NOTIFICATION_ID, notification)
    }

    private fun showAppointmentReminder(context: Context, title: String, time: String) {
        val intent = Intent(context, com.example.senioroslauncher.ui.calendar.CalendarActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SeniorLauncherApp.CHANNEL_APPOINTMENTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Upcoming: $title")
            .setContentText(if (time.isNotEmpty()) "At $time" else "Coming up soon")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(title.hashCode(), notification)
    }

    companion object {
        const val ACTION_HYDRATION_REMINDER = "com.example.senioroslauncher.HYDRATION_REMINDER"
        const val ACTION_APPOINTMENT_REMINDER = "com.example.senioroslauncher.APPOINTMENT_REMINDER"

        const val EXTRA_APPOINTMENT_TITLE = "appointment_title"
        const val EXTRA_APPOINTMENT_TIME = "appointment_time"

        private const val HYDRATION_NOTIFICATION_ID = 2001
    }
}
