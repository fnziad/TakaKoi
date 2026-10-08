package com.example.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Minimal Dark Color Scheme
fun getMinimalDarkColorScheme(colorTheme: String): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFFA9D8E2),
        onPrimary = Color(0xFF0B1F29),
        primaryContainer = Color(0xFF173845),
        onPrimaryContainer = Color(0xFFD6F0F5),
        secondary = Color(0xFF7CD6B9),
        onSecondary = Color(0xFF09241C),
        tertiary = Color(0xFFF4C977),
        background = Color(0xFF101416),
        surface = Color(0xFF171D20),
        surfaceVariant = Color(0xFF222A2E),
        onBackground = Color(0xFFF3F5F4),
        onSurface = Color(0xFFF3F5F4),
        onSurfaceVariant = Color(0xFFB8C2C4),
        outline = Color(0xFF3B484D),
        outlineVariant = Color(0xFF2B3539)
    )
}

// Light Color Schemes per preset — premium warm palette
fun getLightColorScheme(colorTheme: String): ColorScheme {
    return lightColorScheme(
        primary = PremiumViolet,
        onPrimary = Color.White,
        primaryContainer = PremiumVioletSoft,
        onPrimaryContainer = PremiumBlack,
        secondary = PremiumEmerald,
        onSecondary = Color.White,
        tertiary = PremiumAmber,
        background = PremiumBg,
        surface = PremiumSurface,
        surfaceVariant = Color(0xFFF3F1EE),
        onBackground = PremiumBlack,
        onSurface = PremiumBlack,
        onSurfaceVariant = PremiumSlate,
        outline = PremiumBorder,
        outlineVariant = PremiumBorder
    )
}

@Composable
fun TakaTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: String = "INDIGO",
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        getMinimalDarkColorScheme(colorTheme)
    } else {
        getLightColorScheme(colorTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
