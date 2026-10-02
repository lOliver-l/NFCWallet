package com.nfcwallet.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = iOSSystemBlue,
    onPrimary = Color.White,
    primaryContainer = iOSTertiaryGroupedDark,
    onPrimaryContainer = Color.White,
    secondary = iOSSystemIndigo,
    onSecondary = Color.White,
    tertiary = iOSSystemOrange,
    background = iOSSystemBlack,
    surface = iOSSecondaryGroupedDark,
    surfaceVariant = iOSTertiaryGroupedDark,
    outlineVariant = iOSBorderDark,
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF8E8E93)
)

private val LightColorScheme = lightColorScheme(
    primary = iOSSystemBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5EA),
    onPrimaryContainer = iOSLightOnSurface,
    secondary = iOSSystemIndigo,
    onSecondary = Color.White,
    tertiary = iOSSystemOrange,
    background = iOSLightBackground,
    surface = iOSLightSurface,
    surfaceVariant = iOSLightSurfaceVariant,
    onBackground = iOSLightOnSurface,
    onSurface = iOSLightOnSurface,
    onSurfaceVariant = Color(0xFF8E8E93)
)

@Composable
fun NFCWalletTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
