package com.daeumgil.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.daeumgil.app.domain.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF235D49),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4ECD6),
    onPrimaryContainer = Color(0xFF092F20),
    background = Color(0xFFFAFCF9),
    onBackground = Color(0xFF191D1A),
    surface = Color(0xFFFAFCF9),
    onSurface = Color(0xFF191D1A),
    surfaceVariant = Color(0xFFE0E8DF),
    onSurfaceVariant = Color(0xFF414A43),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD5B8),
    onPrimary = Color(0xFF073A27),
    primaryContainer = Color(0xFF20513B),
    onPrimaryContainer = Color(0xFFC4ECD6),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE1E7DF),
    surface = Color(0xFF101512),
    onSurface = Color(0xFFE1E7DF),
    surfaceVariant = Color(0xFF414A43),
    onSurfaceVariant = Color(0xFFC0C9C0),
)

@Composable
fun DaeumgilTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (mode.isDark(isSystemInDarkTheme())) DarkColors else LightColors,
        content = content,
    )
}

