package com.example.senioroslauncher.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senioroslauncher.SeniorLauncherApp
import com.example.senioroslauncher.data.preferences.PreferencesManager
import com.example.senioroslauncher.services.FallDetectionService
import com.example.senioroslauncher.ui.components.LargeListItem
import com.example.senioroslauncher.ui.components.LargeSettingsSwitch
import com.example.senioroslauncher.ui.components.SeniorTopAppBar
import com.example.senioroslauncher.ui.theme.*
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorLauncherTheme {
                SettingsScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Collect settings
    val hearingAidMode by prefsManager.hearingAidMode.collectAsStateWithLifecycle(initialValue = false)
    val voiceFeedback by prefsManager.voiceFeedback.collectAsStateWithLifecycle(initialValue = false)
    val antiShake by prefsManager.antiShake.collectAsStateWithLifecycle(initialValue = false)
    val doubleTapConfirm by prefsManager.doubleTapConfirm.collectAsStateWithLifecycle(initialValue = false)
    val touchVibration by prefsManager.touchVibration.collectAsStateWithLifecycle(initialValue = true)
    val fallDetection by prefsManager.fallDetection.collectAsStateWithLifecycle(initialValue = false)
    val locationSharing by prefsManager.locationSharing.collectAsStateWithLifecycle(initialValue = false)
    val autoAnswerCalls by prefsManager.autoAnswerCalls.collectAsStateWithLifecycle(initialValue = false)
    val language by prefsManager.language.collectAsStateWithLifecycle(initialValue = "en")

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SeniorTopAppBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Accessibility Section
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LargeSettingsSwitch(
                title = "Hearing Aid Mode",
                description = "Optimize audio for hearing aids",
                checked = hearingAidMode,
                onCheckedChange = {
                    scope.launch { prefsManager.setHearingAidMode(it) }
                },
                icon = Icons.Default.Hearing
            )

            LargeSettingsSwitch(
                title = "Voice Feedback",
                description = "Speak actions aloud",
                checked = voiceFeedback,
                onCheckedChange = {
                    scope.launch { prefsManager.setVoiceFeedback(it) }
                },
                icon = Icons.Default.RecordVoiceOver
            )

            LargeSettingsSwitch(
                title = "Anti-Shake",
                description = "Ignore accidental touches",
                checked = antiShake,
                onCheckedChange = {
                    scope.launch { prefsManager.setAntiShake(it) }
                },
                icon = Icons.Default.DoNotTouch
            )

            LargeSettingsSwitch(
                title = "Double-Tap to Confirm",
                description = "Require double tap for actions",
                checked = doubleTapConfirm,
                onCheckedChange = {
                    scope.launch { prefsManager.setDoubleTapConfirm(it) }
                },
                icon = Icons.Default.TouchApp
            )

            LargeSettingsSwitch(
                title = "Touch Vibration",
                description = "Vibrate on button press",
                checked = touchVibration,
                onCheckedChange = {
                    scope.launch { prefsManager.setTouchVibration(it) }
                },
                icon = Icons.Default.Vibration
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Safety Section
            Text(
                text = "Safety",
                style = MaterialTheme.typography.titleLarge,
                color = EmergencyRed,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LargeSettingsSwitch(
                title = "Fall Detection",
                description = "Auto-detect falls and alert contacts",
                checked = fallDetection,
                onCheckedChange = {
                    scope.launch {
                        prefsManager.setFallDetection(it)
                        if (it) {
                            // Start fall detection service
                            val intent = Intent(context, FallDetectionService::class.java)
                            context.startForegroundService(intent)
                        } else {
                            // Stop fall detection service
                            val intent = Intent(context, FallDetectionService::class.java)
                            context.stopService(intent)
                        }
                    }
                },
                icon = Icons.Default.PersonOff
            )

            LargeSettingsSwitch(
                title = "Location Sharing",
                description = "Share location with emergency contacts",
                checked = locationSharing,
                onCheckedChange = {
                    scope.launch { prefsManager.setLocationSharing(it) }
                },
                icon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calls Section
            Text(
                text = "Calls",
                style = MaterialTheme.typography.titleLarge,
                color = PhoneGreen,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LargeSettingsSwitch(
                title = "Auto-Answer Calls",
                description = "Automatically answer incoming calls",
                checked = autoAnswerCalls,
                onCheckedChange = {
                    scope.launch { prefsManager.setAutoAnswerCalls(it) }
                },
                icon = Icons.Default.CallReceived
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Language Section
            Text(
                text = "Language",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryBlue,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LargeListItem(
                title = "App Language",
                subtitle = getLanguageName(language),
                onClick = { showLanguageDialog = true },
                leadingIcon = Icons.Default.Language,
                leadingIconColor = PrimaryBlue,
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MediumGray
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Emergency Contacts Section
            Text(
                text = "Emergency",
                style = MaterialTheme.typography.titleLarge,
                color = EmergencyRed,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LargeListItem(
                title = "Emergency Contacts",
                subtitle = "Manage your emergency contacts",
                onClick = {
                    context.startActivity(Intent(context, EmergencyContactsSettingsActivity::class.java))
                },
                leadingIcon = Icons.Default.ContactPhone,
                leadingIconColor = EmergencyRed,
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MediumGray
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SeniorLauncher",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Designed with care for seniors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        val languages = listOf(
            "en" to "English",
            "hi" to "हिंदी (Hindi)",
            "ta" to "தமிழ் (Tamil)",
            "te" to "తెలుగు (Telugu)"
        )

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == code,
                                onClick = {
                                    scope.launch {
                                        prefsManager.setLanguage(code)
                                    }
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getLanguageName(code: String): String = when (code) {
    "en" -> "English"
    "hi" -> "हिंदी (Hindi)"
    "ta" -> "தமிழ் (Tamil)"
    "te" -> "తెలుగు (Telugu)"
    else -> "English"
}
