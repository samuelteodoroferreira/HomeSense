package com.homesense.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = HomesenseCyan,
    onPrimary = Color(0xFF0D1117),
    primaryContainer = HomesenseCyanDim,
    onPrimaryContainer = Color(0xFF0D1117),
    secondary = HomesenseHumidityAccent,
    onSecondary = Color(0xFF0D1117),
    tertiary = HomesenseChipPurple,
    onTertiary = Color.White,
    background = HomesenseBackground,
    onBackground = HomesenseTextPrimary,
    surface = HomesenseSurface,
    onSurface = HomesenseTextPrimary,
    surfaceVariant = HomesenseSurface,
    onSurfaceVariant = HomesenseTextSecondary,
    outline = HomesenseDivider,
    error = Color(0xFFFF6B6B),
    onError = Color.Black,
)

@Composable
fun HomesenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = HomesenseTypography,
        content = content,
    )
}
