package com.example.senioroslauncher.ui.health

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senioroslauncher.SeniorLauncherApp
import com.example.senioroslauncher.ui.components.LargeListItem
import com.example.senioroslauncher.ui.components.SeniorTopAppBar
import com.example.senioroslauncher.ui.medication.MedicationActivity
import com.example.senioroslauncher.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

class HealthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorLauncherTheme {
                HealthScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as SeniorLauncherApp
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Get today's hydration log
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.time

    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.time

    val hydrationLog by app.database.hydrationLogDao()
        .getTodayLog(startOfDay, endOfDay)
        .collectAsStateWithLifecycle(initialValue = null)

    val glassesCount = hydrationLog?.glassesCount ?: 0
    val hydrationGoal = hydrationLog?.goal ?: 8

    Scaffold(
        topBar = {
            SeniorTopAppBar(
                title = "Health",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Water Intake Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Water Intake",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VeryDarkGray
                        )
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress
                    Text(
                        text = "$glassesCount of $hydrationGoal glasses",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (glassesCount.toFloat() / hydrationGoal).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = PrimaryBlue,
                        trackColor = White,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Decrease Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
                                    val start = cal.time
                                    cal.add(Calendar.DAY_OF_MONTH, 1)
                                    val end = cal.time
                                    app.database.hydrationLogDao().decrementGlasses(start, end)
                                }
                            },
                            modifier = Modifier.height(56.dp),
                            enabled = glassesCount > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove glass",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Add Button
                        Button(
                            onClick = {
                                scope.launch {
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
                                    val start = cal.time
                                    cal.add(Calendar.DAY_OF_MONTH, 1)
                                    val end = cal.time
                                    app.database.hydrationLogDao().incrementGlasses(start, end)
                                }
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Glass",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Decrease Button (invisible for balance)
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
                                    val start = cal.time
                                    cal.add(Calendar.DAY_OF_MONTH, 1)
                                    val end = cal.time
                                    app.database.hydrationLogDao().decrementGlasses(start, end)
                                }
                            },
                            modifier = Modifier.height(56.dp),
                            enabled = glassesCount > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove glass",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Quick Access Cards
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            LargeListItem(
                title = "Medical Profile",
                subtitle = "Blood type, allergies, conditions",
                onClick = {
                    context.startActivity(Intent(context, MedicalProfileActivity::class.java))
                },
                leadingIcon = Icons.Default.Person,
                leadingIconColor = HealthRed,
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MediumGray
                    )
                }
            )

            LargeListItem(
                title = "Medications",
                subtitle = "View and manage medications",
                onClick = {
                    context.startActivity(Intent(context, MedicationActivity::class.java))
                },
                leadingIcon = Icons.Default.Medication,
                leadingIconColor = MedicationOrange,
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MediumGray
                    )
                }
            )

            // Health Tips Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SecondaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Health Tip",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stay hydrated! Drinking enough water helps maintain energy levels and keeps your body functioning well.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkGray
                    )
                }
            }
        }
    }
}
