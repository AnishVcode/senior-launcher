package com.example.senioroslauncher.ui.health

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senioroslauncher.SeniorLauncherApp
import com.example.senioroslauncher.data.database.entity.MedicalProfileEntity
import com.example.senioroslauncher.ui.components.SeniorTopAppBar
import com.example.senioroslauncher.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Date

class MedicalProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorLauncherTheme {
                MedicalProfileScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalProfileScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as SeniorLauncherApp
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val profile by app.database.medicalProfileDao()
        .getProfile()
        .collectAsStateWithLifecycle(initialValue = null)

    var bloodType by remember(profile) { mutableStateOf(profile?.bloodType ?: "") }
    var allergies by remember(profile) { mutableStateOf(profile?.allergies ?: "") }
    var medicalConditions by remember(profile) { mutableStateOf(profile?.medicalConditions ?: "") }
    var emergencyNotes by remember(profile) { mutableStateOf(profile?.emergencyNotes ?: "") }
    var doctorName by remember(profile) { mutableStateOf(profile?.doctorName ?: "") }
    var doctorPhone by remember(profile) { mutableStateOf(profile?.doctorPhone ?: "") }
    var insuranceInfo by remember(profile) { mutableStateOf(profile?.insuranceInfo ?: "") }

    var hasChanges by remember { mutableStateOf(false) }

    // Track changes
    LaunchedEffect(bloodType, allergies, medicalConditions, emergencyNotes, doctorName, doctorPhone, insuranceInfo) {
        hasChanges = profile?.let {
            bloodType != it.bloodType ||
                    allergies != it.allergies ||
                    medicalConditions != it.medicalConditions ||
                    emergencyNotes != it.emergencyNotes ||
                    doctorName != it.doctorName ||
                    doctorPhone != it.doctorPhone ||
                    insuranceInfo != it.insuranceInfo
        } ?: (bloodType.isNotEmpty() || allergies.isNotEmpty() || medicalConditions.isNotEmpty() ||
                emergencyNotes.isNotEmpty() || doctorName.isNotEmpty() || doctorPhone.isNotEmpty() ||
                insuranceInfo.isNotEmpty())
    }

    Scaffold(
        topBar = {
            SeniorTopAppBar(
                title = "Medical Profile",
                onBackClick = onBackClick,
                actions = {
                    if (hasChanges) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    app.database.medicalProfileDao().saveProfile(
                                        MedicalProfileEntity(
                                            bloodType = bloodType,
                                            allergies = allergies,
                                            medicalConditions = medicalConditions,
                                            emergencyNotes = emergencyNotes,
                                            doctorName = doctorName,
                                            doctorPhone = doctorPhone,
                                            insuranceInfo = insuranceInfo,
                                            updatedAt = Date()
                                        )
                                    )
                                    hasChanges = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Blood Type
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Bloodtype,
                            contentDescription = null,
                            tint = EmergencyRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Blood Type",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., A+, B-, O+") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Allergies
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allergies",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("List any allergies (medications, food, etc.)") },
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Medical Conditions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = HealthRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Medical Conditions",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = medicalConditions,
                        onValueChange = { medicalConditions = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Diabetes, Heart condition, etc.") },
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Emergency Notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardRed)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = null,
                            tint = EmergencyRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Notes",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emergencyNotes,
                        onValueChange = { emergencyNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Important information for emergency responders") },
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Doctor Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Doctor Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Doctor's Name") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = doctorPhone,
                        onValueChange = { doctorPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Doctor's Phone") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Insurance Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = SecondaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Insurance Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = insuranceInfo,
                        onValueChange = { insuranceInfo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Insurance provider, policy number, etc.") },
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Save Button
            if (hasChanges) {
                Button(
                    onClick = {
                        scope.launch {
                            app.database.medicalProfileDao().saveProfile(
                                MedicalProfileEntity(
                                    bloodType = bloodType,
                                    allergies = allergies,
                                    medicalConditions = medicalConditions,
                                    emergencyNotes = emergencyNotes,
                                    doctorName = doctorName,
                                    doctorPhone = doctorPhone,
                                    insuranceInfo = insuranceInfo,
                                    updatedAt = Date()
                                )
                            )
                            hasChanges = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
