package com.example.senioroslauncher.ui.ride

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.senioroslauncher.data.preferences.PreferencesManager
import com.example.senioroslauncher.ui.components.SeniorTopAppBar
import com.example.senioroslauncher.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RideBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorLauncherTheme {
                RideBookingScreen(onBackClick = { finish() })
            }
        }
    }
}

data class RideService(
    val name: String,
    val packageName: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideBookingScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }

    // Saved addresses state
    var homeAddress by remember { mutableStateOf("") }
    var doctorAddress by remember { mutableStateOf("") }
    var pharmacyAddress by remember { mutableStateOf("") }

    // Dialog states
    var showAddressDialog by remember { mutableStateOf(false) }
    var editingPlaceType by remember { mutableStateOf("") }
    var editingAddress by remember { mutableStateOf("") }

    // Load saved addresses
    LaunchedEffect(Unit) {
        preferencesManager.homeAddress.collect { homeAddress = it }
    }
    LaunchedEffect(Unit) {
        preferencesManager.doctorAddress.collect { doctorAddress = it }
    }
    LaunchedEffect(Unit) {
        preferencesManager.pharmacyAddress.collect { pharmacyAddress = it }
    }

    val rideServices = remember {
        listOf(
            RideService("Uber", "com.ubercab", Color(0xFF000000), Icons.Default.DirectionsCar),
            RideService("Lyft", "me.lyft.android", Color(0xFFFF00BF), Icons.Default.LocalTaxi),
            RideService("Ola", "com.olacabs.customer", Color(0xFF84BD00), Icons.Default.DirectionsCar),
            RideService("Rapido", "com.rapido.passenger", Color(0xFFFFC107), Icons.Default.TwoWheeler)
        )
    }

    Scaffold(
        topBar = {
            SeniorTopAppBar(
                title = "Book a Ride",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Ride Services
            Text(
                text = "Ride Services",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rideServices.take(2).forEach { service ->
                    RideServiceCard(
                        service = service,
                        isInstalled = isAppInstalled(context, service.packageName),
                        onClick = { launchRideService(context, service) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rideServices.drop(2).forEach { service ->
                    RideServiceCard(
                        service = service,
                        isInstalled = isAppInstalled(context, service.packageName),
                        onClick = { launchRideService(context, service) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rideServices.size % 2 != 0) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Call Taxi
            Card(
                onClick = {
                    // Generic taxi call - you can customize this
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardYellow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = RideYellow
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = VeryDarkGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Call a Taxi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Call your local taxi service",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Saved Places
            Text(
                text = "Saved Places",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SavedPlaceCard(
                icon = Icons.Default.Home,
                title = "Home",
                subtitle = if (homeAddress.isNotEmpty()) homeAddress else "Tap to set address",
                color = PrimaryBlue,
                hasAddress = homeAddress.isNotEmpty(),
                onClick = {
                    if (homeAddress.isNotEmpty()) {
                        openRideAppWithDestination(context, homeAddress)
                    } else {
                        editingPlaceType = "Home"
                        editingAddress = homeAddress
                        showAddressDialog = true
                    }
                },
                onLongClick = {
                    editingPlaceType = "Home"
                    editingAddress = homeAddress
                    showAddressDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SavedPlaceCard(
                icon = Icons.Default.LocalHospital,
                title = "Doctor",
                subtitle = if (doctorAddress.isNotEmpty()) doctorAddress else "Tap to set address",
                color = HealthRed,
                hasAddress = doctorAddress.isNotEmpty(),
                onClick = {
                    if (doctorAddress.isNotEmpty()) {
                        openRideAppWithDestination(context, doctorAddress)
                    } else {
                        editingPlaceType = "Doctor"
                        editingAddress = doctorAddress
                        showAddressDialog = true
                    }
                },
                onLongClick = {
                    editingPlaceType = "Doctor"
                    editingAddress = doctorAddress
                    showAddressDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SavedPlaceCard(
                icon = Icons.Default.LocalPharmacy,
                title = "Pharmacy",
                subtitle = if (pharmacyAddress.isNotEmpty()) pharmacyAddress else "Tap to set address",
                color = SecondaryGreen,
                hasAddress = pharmacyAddress.isNotEmpty(),
                onClick = {
                    if (pharmacyAddress.isNotEmpty()) {
                        openRideAppWithDestination(context, pharmacyAddress)
                    } else {
                        editingPlaceType = "Pharmacy"
                        editingAddress = pharmacyAddress
                        showAddressDialog = true
                    }
                },
                onLongClick = {
                    editingPlaceType = "Pharmacy"
                    editingAddress = pharmacyAddress
                    showAddressDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Safety Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardGreen)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SecondaryGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Safety Tips",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Share your trip with a family member\n• Check the driver's photo and license plate\n• Sit in the back seat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGray
                        )
                    }
                }
            }
        }
    }

    // Address Edit Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = {
                Text(
                    text = "Set $editingPlaceType Address",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the address for $editingPlaceType:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = editingAddress,
                        onValueChange = { editingAddress = it },
                        label = { Text("Address") },
                        placeholder = { Text("e.g., 123 Main St, City") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (editingPlaceType) {
                                "Home" -> {
                                    preferencesManager.setHomeAddress(editingAddress)
                                    homeAddress = editingAddress
                                }
                                "Doctor" -> {
                                    preferencesManager.setDoctorAddress(editingAddress)
                                    doctorAddress = editingAddress
                                }
                                "Pharmacy" -> {
                                    preferencesManager.setPharmacyAddress(editingAddress)
                                    pharmacyAddress = editingAddress
                                }
                            }
                        }
                        showAddressDialog = false
                        Toast.makeText(context, "$editingPlaceType address saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Save", style = MaterialTheme.typography.titleMedium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddressDialog = false },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.titleMedium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun RideServiceCard(
    service: RideService,
    isInstalled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) service.color.copy(alpha = 0.1f) else LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isInstalled) service.color else MediumGray
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isInstalled) service.color else DarkGray
            )
            if (!isInstalled) {
                Text(
                    text = "Install",
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGray
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedPlaceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    hasAddress: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasAddress) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (hasAddress) {
                    Text(
                        text = "Long press to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MediumGray
                    )
                }
            }
            Icon(
                imageVector = if (hasAddress) Icons.Default.Navigation else Icons.Default.Edit,
                contentDescription = null,
                tint = if (hasAddress) color else MediumGray
            )
        }
    }
}

private fun openRideAppWithDestination(context: Context, address: String) {
    val encodedAddress = Uri.encode(address)

    // Try Uber first
    val uberIntent = try {
        val uri = Uri.parse("uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=$encodedAddress")
        Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.ubercab")
        }
    } catch (e: Exception) { null }

    if (uberIntent != null && uberIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(uberIntent)
        return
    }

    // Try Ola
    val olaIntent = try {
        val uri = Uri.parse("olacabs://app/launch?landing_page=bk&drop_address=$encodedAddress")
        Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.olacabs.customer")
        }
    } catch (e: Exception) { null }

    if (olaIntent != null && olaIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(olaIntent)
        return
    }

    // Try Lyft
    val lyftIntent = try {
        val uri = Uri.parse("lyft://ridetype?id=lyft&destination[address]=$encodedAddress")
        Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("me.lyft.android")
        }
    } catch (e: Exception) { null }

    if (lyftIntent != null && lyftIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(lyftIntent)
        return
    }

    // Try Rapido
    val rapidoIntent = try {
        Intent(Intent.ACTION_VIEW, Uri.parse("rapido://")).apply {
            setPackage("com.rapido.passenger")
        }
    } catch (e: Exception) { null }

    if (rapidoIntent != null && rapidoIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(rapidoIntent)
        Toast.makeText(context, "Enter destination: $address", Toast.LENGTH_LONG).show()
        return
    }

    // No ride app installed - show message and offer to install
    Toast.makeText(context, "No ride app installed. Please install Uber, Ola, Lyft, or Rapido.", Toast.LENGTH_LONG).show()

    // Open Play Store to install Uber
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ubercab")))
    } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab")))
    }
}

private fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

private fun launchRideService(context: Context, service: RideService) {
    val intent = context.packageManager.getLaunchIntentForPackage(service.packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        // Open Play Store
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${service.packageName}")))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${service.packageName}")))
        }
    }
}
