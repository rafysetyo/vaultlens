package com.vaultlens.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Legacy names, still referenced by other files.
val DeepSlate950 = Color(0xFF0B1120)
val DeepSlate900 = Color(0xFF111827)
val DeepSlate800 = Color(0xFF1E293B)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val Indigo600 = Color(0xFF4F46E5)
val Indigo400 = Color(0xFF818CF8)
val Emerald600 = Color(0xFF059669)
val Emerald400 = Color(0xFF34D399)

// New palette (soft lavender fintech look).
val Ink = Color(0xFF17171C)
val Canvas = Color(0xFFF1F1F4)
val Lavender = Color(0xFFEDE7F6)
val Violet = Color(0xFF7C5CBF)
val VioletDeep = Color(0xFF4A3668)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD9CCF2),
    onPrimary = Color(0xFF1E1530),
    primaryContainer = Color(0xFF3A2D57),
    onPrimaryContainer = Color(0xFFEDE7F6),
    secondary = Emerald400,
    onSecondary = Color(0xFF06241A),
    secondaryContainer = Color(0xFF3A2D57),
    onSecondaryContainer = Color(0xFFEDE7F6),
    tertiary = Color(0xFFB79CF0),
    background = Color(0xFF0F0D14),
    onBackground = Color(0xFFF2F0F7),
    surface = Color(0xFF1A1722),
    onSurface = Color(0xFFF2F0F7),
    surfaceVariant = Color(0xFF24202F),
    onSurfaceVariant = Color(0xFFA39FB3),
    outline = Color(0xFF332E40)
)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Lavender,
    onPrimaryContainer = Color(0xFF2D1F4A),
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Lavender,
    onSecondaryContainer = Color(0xFF2D1F4A),
    tertiary = Violet,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF4F2F8),
    onSurfaceVariant = Color(0xFF6B6B78),
    outline = Color(0xFFE4E4EA)
)

private val VaultTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VaultLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = VaultTypography,
        shapes = VaultShapes,
        content = content
    )
}
