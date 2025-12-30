package com.example.senioroslauncher.ui.video

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.senioroslauncher.ui.components.SeniorTopAppBar
import com.example.senioroslauncher.ui.theme.*

class VideoContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeniorLauncherTheme {
                VideoContactsScreen(onBackClick = { finish() })
            }
        }
    }
}

data class VideoApp(
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val color: Color,
    val deepLink: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoContactsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    val videoApps = remember {
        listOf(
            VideoApp("WhatsApp", "com.whatsapp", Icons.Default.Chat, Color(0xFF25D366)),
            VideoApp("Zoom", "us.zoom.videomeetings", Icons.Default.Groups, Color(0xFF2D8CFF)),
            VideoApp("Google Meet", "com.google.android.apps.meetings", Icons.Default.VideoChat, Color(0xFF00897B)),
            VideoApp("Skype", "com.skype.raider", Icons.Default.VideoCall, Color(0xFF00AFF0)),
            VideoApp("FaceTime", "com.apple.facetime", Icons.Default.Face, Color(0xFF34C759)),
            VideoApp("Duo/Meet", "com.google.android.apps.tachyon", Icons.Default.Duo, Color(0xFF1A73E8))
        )
    }

    Scaffold(
        topBar = {
            SeniorTopAppBar(
                title = "Video Call",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Choose a video calling app",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videoApps) { app ->
                    VideoAppCard(
                        app = app,
                        isInstalled = isAppInstalled(context, app.packageName),
                        onClick = {
                            launchVideoApp(context, app)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tap on an app to start a video call. Apps that aren't installed will open the Play Store.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoAppCard(
    app: VideoApp,
    isInstalled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) app.color.copy(alpha = 0.1f) else LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (isInstalled) app.color else MediumGray
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = app.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isInstalled) app.color else DarkGray
                )
                Text(
                    text = if (isInstalled) "Tap to open" else "Not installed - tap to download",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isInstalled) DarkGray else MediumGray
                )
            }

            // Arrow
            Icon(
                imageVector = if (isInstalled) Icons.Default.ChevronRight else Icons.Default.Download,
                contentDescription = null,
                tint = if (isInstalled) app.color else MediumGray,
                modifier = Modifier.size(28.dp)
            )
        }
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

private fun launchVideoApp(context: Context, app: VideoApp) {
    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        // Open Play Store
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}")))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")))
        }
    }
}
