package com.example.senioroslauncher.services

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import com.example.senioroslauncher.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallAutoAnswerService : InCallService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val prefsManager = PreferencesManager(applicationContext)

        serviceScope.launch {
            val autoAnswer = prefsManager.autoAnswerCalls.first()
            if (autoAnswer && call.state == Call.STATE_RINGING) {
                // Auto answer the call
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                }
            }
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
    }
}
