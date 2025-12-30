package com.example.senioroslauncher.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.senioroslauncher.SeniorLauncherApp
import com.example.senioroslauncher.receivers.MedicationAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class MedicationReminderService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SCHEDULE_ALL -> scheduleAllReminders()
            ACTION_SCHEDULE_ONE -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                if (medicationId != -1L) {
                    scheduleRemindersForMedication(medicationId)
                }
            }
            ACTION_CANCEL_ONE -> {
                val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
                if (medicationId != -1L) {
                    cancelRemindersForMedication(medicationId)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun scheduleAllReminders() {
        serviceScope.launch {
            val app = applicationContext as SeniorLauncherApp
            val schedules = app.database.medicationScheduleDao().getAllEnabledSchedules()

            schedules.forEach { schedule ->
                val medication = app.database.medicationDao().getMedicationById(schedule.medicationId)
                medication?.let {
                    scheduleAlarm(
                        scheduleId = schedule.id,
                        medicationId = it.id,
                        medicationName = it.name,
                        medicationDosage = it.dosage,
                        hour = schedule.hour,
                        minute = schedule.minute
                    )
                }
            }
        }
    }

    private fun scheduleRemindersForMedication(medicationId: Long) {
        serviceScope.launch {
            val app = applicationContext as SeniorLauncherApp
            val medication = app.database.medicationDao().getMedicationById(medicationId)
            val schedules = app.database.medicationScheduleDao().getSchedulesForMedicationSync(medicationId)

            medication?.let { med ->
                schedules.forEach { schedule ->
                    scheduleAlarm(
                        scheduleId = schedule.id,
                        medicationId = med.id,
                        medicationName = med.name,
                        medicationDosage = med.dosage,
                        hour = schedule.hour,
                        minute = schedule.minute
                    )
                }
            }
        }
    }

    private fun cancelRemindersForMedication(medicationId: Long) {
        serviceScope.launch {
            val app = applicationContext as SeniorLauncherApp
            val schedules = app.database.medicationScheduleDao().getSchedulesForMedicationSync(medicationId)

            schedules.forEach { schedule ->
                cancelAlarm(schedule.id)
            }
        }
    }

    private fun scheduleAlarm(
        scheduleId: Long,
        medicationId: Long,
        medicationName: String,
        medicationDosage: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, MedicationAlarmReceiver::class.java).apply {
            action = MedicationAlarmReceiver.ACTION_MEDICATION_REMINDER
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fall back to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(scheduleId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val ACTION_SCHEDULE_ALL = "com.example.senioroslauncher.SCHEDULE_ALL_MEDICATIONS"
        const val ACTION_SCHEDULE_ONE = "com.example.senioroslauncher.SCHEDULE_ONE_MEDICATION"
        const val ACTION_CANCEL_ONE = "com.example.senioroslauncher.CANCEL_ONE_MEDICATION"
        const val EXTRA_MEDICATION_ID = "medication_id"

        fun scheduleAllReminders(context: Context) {
            val intent = Intent(context, MedicationReminderService::class.java).apply {
                action = ACTION_SCHEDULE_ALL
            }
            context.startService(intent)
        }

        fun scheduleMedicationReminders(context: Context, medicationId: Long) {
            val intent = Intent(context, MedicationReminderService::class.java).apply {
                action = ACTION_SCHEDULE_ONE
                putExtra(EXTRA_MEDICATION_ID, medicationId)
            }
            context.startService(intent)
        }

        fun cancelMedicationReminders(context: Context, medicationId: Long) {
            val intent = Intent(context, MedicationReminderService::class.java).apply {
                action = ACTION_CANCEL_ONE
                putExtra(EXTRA_MEDICATION_ID, medicationId)
            }
            context.startService(intent)
        }
    }
}
