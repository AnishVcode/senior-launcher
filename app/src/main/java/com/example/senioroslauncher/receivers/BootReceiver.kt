package com.example.senioroslauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.senioroslauncher.data.preferences.PreferencesManager
import com.example.senioroslauncher.services.FallDetectionService
import com.example.senioroslauncher.services.MedicationReminderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Reschedule all medication reminders
            MedicationReminderService.scheduleAllReminders(context)

            // Check if fall detection should be started
            scope.launch {
                val prefsManager = PreferencesManager(context)
                val fallDetectionEnabled = prefsManager.fallDetection.first()

                if (fallDetectionEnabled) {
                    val serviceIntent = Intent(context, FallDetectionService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}
