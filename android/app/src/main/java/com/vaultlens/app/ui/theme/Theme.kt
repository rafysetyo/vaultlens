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

private val DarkColors = darkColorScheme(
    primary = Indigo400,
    onPrimary = DeepSlate950,
    secondary = Emerald400,
    onSecondary = DeepSlate950,
    background = DeepSlate950,
    onBackground = Slate100,
    surface = DeepSlate900,
    onSurface = Slate100,
    surfaceVariant = DeepSlate800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val LightColors = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    secondary = Emerald600,
    onSecondary = Color.White,
    background = Slate50,
    onBackground = DeepSlate900,
    surface = Color.White,
    onSurface = DeepSlate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

private val VaultTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
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
