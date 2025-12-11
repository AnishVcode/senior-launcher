package com.example.senioroslauncher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = CardBlue,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SecondaryGreen,
    onSecondary = White,
    secondaryContainer = CardGreen,
    onSecondaryContainer = SecondaryGreen,
    tertiary = MedicationOrange,
    onTertiary = White,
    tertiaryContainer = CardOrange,
    onTertiaryContainer = Color(0xFFE65100),
    error = EmergencyRed,
    onError = White,
    errorContainer = CardRed,
    onErrorContainer = EmergencyRedDark,
    background = BackgroundLight,
    onBackground = VeryDarkGray,
    surface = SurfaceLight,
    onSurface = VeryDarkGray,
    surfaceVariant = OffWhite,
    onSurfaceVariant = DarkGray,
    outline = MediumGray,
    outlineVariant = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = VeryDarkGray,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = CardBlue,
    secondary = SecondaryGreenLight,
    onSecondary = VeryDarkGray,
    secondaryContainer = SecondaryGreen,
    onSecondaryContainer = CardGreen,
    tertiary = WarningOrange,
    onTertiary = VeryDarkGray,
    tertiaryContainer = Color(0xFFE65100),
    onTertiaryContainer = CardOrange,
    error = EmergencyRedLight,
    onError = VeryDarkGray,
    errorContainer = EmergencyRedDark,
    onErrorContainer = CardRed,
    background = BackgroundDark,
    onBackground = OffWhite,
    surface = SurfaceDark,
    onSurface = OffWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    outlineVariant = DarkGray
)

@Composable
fun SeniorLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep the old name for backwards compatibility
@Composable
fun SenioroslauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SeniorLauncherTheme(darkTheme = darkTheme, content = content)
}
