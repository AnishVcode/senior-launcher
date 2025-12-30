package com.example.senioroslauncher.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.senioroslauncher.R
import com.example.senioroslauncher.SeniorLauncherApp
import com.example.senioroslauncher.data.database.entity.MedicationAction
import com.example.senioroslauncher.data.database.entity.MedicationLogEntity
import com.example.senioroslauncher.ui.medication.MedicationReminderActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class MedicationAlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            ACTION_MEDICATION_REMINDER -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
                val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
                val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)

                // Show notification
                showMedicationNotification(context, medicationId, medicationName, medicationDosage)

                // Launch full-screen activity
                launchReminderActivity(context, medicationId, medicationName, medicationDosage)

                // Vibrate
                vibrate(context)

                // Reschedule for tomorrow
                rescheduleAlarm(context, scheduleId)
            }

            ACTION_MEDICATION_TAKEN -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                if (medicationId != -1L) {
                    logMedicationAction(context, medicationId, MedicationAction.TAKEN)
                    cancelNotification(context, medicationId)
                }
            }

            ACTION_MEDICATION_SKIPPED -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                if (medicationId != -1L) {
                    logMedicationAction(context, medicationId, MedicationAction.SKIPPED)
                    cancelNotification(context, medicationId)
                }
            }

            ACTION_MEDICATION_SNOOZED -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
                val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""

                if (medicationId != -1L) {
                    logMedicationAction(context, medicationId, MedicationAction.SNOOZED)
                    cancelNotification(context, medicationId)
                    // Schedule snooze reminder for 10 minutes
                    scheduleSnoozeReminder(context, medicationId, medicationName, medicationDosage)
                }
            }
        }
    }

    private fun showMedicationNotification(
        context: Context,
        medicationId: Long,
        medicationName: String,
        medicationDosage: String
    ) {
        val fullScreenIntent = Intent(context, MedicationReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medication_id", medicationId)
            putExtra("medication_name", medicationName)
            putExtra("medication_dosage", medicationDosage)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, medicationId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Taken action
        val takenIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_MEDICATION_TAKEN
            putExtra(EXTRA_MEDICATION_ID, medicationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context, (medicationId * 10 + 1).toInt(), takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_MEDICATION_SNOOZED
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_DOSAGE, medicationDosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, (medicationId * 10 + 2).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SeniorLauncherApp.CHANNEL_MEDICATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time for your medication!")
            .setContentText("$medicationName - $medicationDosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "I took it", takenPendingIntent)
            .addAction(0, "Snooze 10 min", snoozePendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(medicationId.toInt(), notification)
    }

    private fun launchReminderActivity(
        context: Context,
        medicationId: Long,
        medicationName: String,
        medicationDosage: String
    ) {
        val intent = Intent(context, MedicationReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medication_id", medicationId)
            putExtra("medication_name", medicationName)
            putExtra("medication_dosage", medicationDosage)
        }
        context.startActivity(intent)
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500, 200, 500), -1)
        }
    }

    private fun logMedicationAction(context: Context, medicationId: Long, action: MedicationAction) {
        scope.launch {
            val app = context.applicationContext as SeniorLauncherApp
            app.database.medicationLogDao().insert(
                MedicationLogEntity(
                    medicationId = medicationId,
                    scheduledTime = Date(),
                    action = action
                )
            )
        }
    }

    private fun cancelNotification(context: Context, medicationId: Long) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(medicationId.toInt())
    }

    private fun rescheduleAlarm(context: Context, scheduleId: Long) {
        // The MedicationReminderService will handle rescheduling
        // when it processes the next day's alarms
    }

    private fun scheduleSnoozeReminder(
        context: Context,
        medicationId: Long,
        medicationName: String,
        medicationDosage: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_MEDICATION_REMINDER
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(EXTRA_SCHEDULE_ID, -1L) // -1 indicates snooze, don't reschedule
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 100).toInt(), // Different request code for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes

        alarmManager.setAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    companion object {
        const val ACTION_MEDICATION_REMINDER = "com.example.senioroslauncher.MEDICATION_REMINDER"
        const val ACTION_MEDICATION_TAKEN = "com.example.senioroslauncher.MEDICATION_TAKEN"
        const val ACTION_MEDICATION_SKIPPED = "com.example.senioroslauncher.MEDICATION_SKIPPED"
        const val ACTION_MEDICATION_SNOOZED = "com.example.senioroslauncher.MEDICATION_SNOOZED"

        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
    }
}
