package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AgriLightColorScheme = lightColorScheme(
    primary = AgriBlue,
    onPrimary = Color.White,
    primaryContainer = StatusBlueBg,
    onPrimaryContainer = AgriBlue,
    secondary = AgriCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = StatusGreen,
    onTertiary = Color.White,
    background = RoyalWhite,
    onBackground = DarkNavy,
    surface = CardWhite,
    onSurface = DarkNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = NavySecondary,
    outline = BorderLight,
    error = StatusRed,
    onError = Color.White,
    errorContainer = StatusRedBg,
    onErrorContainer = StatusRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgriLightColorScheme,
        typography = Typography,
        content = content
    )
}
