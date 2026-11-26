package com.vaultlens.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DeepSlate950 = Color(0xFF0B1120)
val DeepSlate900 = Color(0xFF111827)
val DeepSlate800 = Color(0xFF1E293B)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val Indigo600 = Color(0xFF4F46E5)
val Indigo400 = Color(0xFF818CF8)
val Emerald600 = Color(0xFF059669)
val Emerald400 = Color(0xFF34D399)

// Minimal Studio palette: ink on white, one green accent for the privacy shield.
val Ink = Color(0xFF111111)
val Paper = Color(0xFFF6F6F6)
val Hairline = Color(0xFFE6E6E6)
val Mute = Color(0xFF6B6B6B)

private val DarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF0A0A0A),
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color.White,
    secondary = Emerald400,
    onSecondary = Color(0xFF0A0A0A),
    secondaryContainer = Color.White,
    onSecondaryContainer = Color(0xFF0A0A0A),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF161616),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF222222),
    onSurfaceVariant = Color(0xFFA3A3A3),
    surfaceContainer = Color(0xFF1C1C1C),
    outline = Color(0xFF2E2E2E),
    outlineVariant = Color(0xFF3A3A3A)
)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDEDED),
    onPrimaryContainer = Ink,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Ink,
    onSecondaryContainer = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Mute,
    surfaceContainer = Color.White,
    outline = Hairline,
    outlineVariant = Color(0xFFD9D9D9)
)

private val VaultTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun VaultLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = VaultTypography,
        content = content
    )
}
