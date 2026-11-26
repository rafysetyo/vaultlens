package com.vaultlens.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Design tokens, copied from app/globals.css (the OKLCH values converted to sRGB).
 * Names follow the CSS variables so the two files are easy to diff.
 *
 * Fixed brand colours (logo, hero card) do not change with the theme.
 * Everything else comes from the :root (light) and .dark (dark) blocks.
 */

// Brand, fixed. chart-1 / chart-2 of the dark theme, plus the dark card and ink tones.
val BrandTeal = Color(0xFF1CC1AF)
val BrandCyan = Color(0xFF50D9EF)
val BrandTile = Color(0xFF11131F)
val BrandInk = Color(0xFFE2E4EF)

// :root (light)
private val LightCanvas = Color(0xFFEEEEEC)
private val LightBackground = Color(0xFFFAFAF9)
private val LightForeground = Color(0xFF111114)
private val LightInk = Color(0xFF111114)
private val LightCard = Color(0xFFFFFFFF)
private val LightPopover = Color(0xFFFFFFFF)
private val LightPrimary = Color(0xFF3C44DF)
private val LightPrimaryForeground = Color(0xFFFCFCFC)
private val LightSecondary = Color(0xFFF4F3F2)
private val LightSecondaryForeground = Color(0xFF1A1A1D)
private val LightMuted = Color(0xFFF4F3F2)
private val LightMutedForeground = Color(0xFF626367)
private val LightAccent = Color(0xFFECEFFF)
private val LightAccentForeground = Color(0xFF2F34B7)
private val LightDestructive = Color(0xFFD02B31)
private val LightBorder = Color(0xFFE4E3E1)
private val LightInput = Color(0xFFE0DFDD)
private val LightChart2 = Color(0xFF8A9FF0)
private val LightErrorContainer = Color(0xFFF9E6E6) // destructive at 12% over card

// .dark
private val DarkCanvas = Color(0xFF03030A)
private val DarkBackground = Color(0xFF090A14)
private val DarkForeground = Color(0xFFEDEEF4)
private val DarkInk = Color(0xFFE2E4EF)
private val DarkCard = Color(0xFF11131F)
private val DarkPopover = Color(0xFF061D1A)
private val DarkPrimary = Color(0xFF1CC1AF)
private val DarkPrimaryForeground = Color(0xFF000C0A)
private val DarkSecondary = Color(0xFF0F2623)
private val DarkSecondaryForeground = Color(0xFFEDEEF4)
private val DarkMuted = Color(0xFF1A1C2A)
private val DarkMutedForeground = Color(0xFF9C9DAB)
private val DarkAccent = Color(0xFF013630)
private val DarkAccentForeground = Color(0xFF90EBDD)
private val DarkDestructive = Color(0xFFFF6367)
private val DarkBorder = Color(0x17FFFFFF) // white 9%
private val DarkInput = Color(0x21FFFFFF) // white 13%
private val DarkChart2 = Color(0xFF50D9EF)
private val DarkErrorContainer = Color(0xFF41232D) // destructive at 20% over card

// Legacy names, kept so any other file that still imports them keeps compiling.
// They now point at the new palette (light-theme values for the neutral ones).
val DeepSlate950 = DarkCanvas
val DeepSlate900 = DarkBackground
val DeepSlate800 = DarkCard
val Slate100 = LightSecondary
val Slate50 = LightBackground
val Indigo600 = LightPrimary
val Indigo400 = LightChart2
val Emerald600 = DarkPrimary
val Emerald400 = DarkAccentForeground
val Lavender = LightAccent
val Violet = LightPrimary
val VioletDeep = LightAccentForeground

val Ink = LightInk
val Canvas = LightCanvas
val Muted = LightMutedForeground
val Hairline = LightBorder
val Fill = LightMuted

/** Tokens that have no slot in the Material colour scheme. Read with `MaterialTheme.vaultExtras`. */
@Immutable
class VaultExtras(
    /** --ink: solid redaction block. */
    val ink: Color,
    /** Text colour drawn on top of [ink]. */
    val onInk: Color,
    /** --canvas: the tone behind the app frame. */
    val canvas: Color
)

private val LightExtras = VaultExtras(ink = LightInk, onInk = LightCard, canvas = LightCanvas)
private val DarkExtras = VaultExtras(ink = DarkInk, onInk = DarkBackground, canvas = DarkCanvas)

val LocalVaultExtras = staticCompositionLocalOf { LightExtras }

val MaterialTheme.vaultExtras: VaultExtras
    @Composable
    @ReadOnlyComposable
    get() = LocalVaultExtras.current

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightPrimaryForeground,
    primaryContainer = LightAccent,
    onPrimaryContainer = LightAccentForeground,
    inversePrimary = DarkPrimary,
    secondary = LightChart2,
    onSecondary = LightForeground,
    secondaryContainer = LightSecondary,
    onSecondaryContainer = LightSecondaryForeground,
    tertiary = LightChart2,
    onTertiary = LightForeground,
    tertiaryContainer = LightAccent,
    onTertiaryContainer = LightAccentForeground,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightCard,
    onSurface = LightForeground,
    surfaceVariant = LightMuted,
    onSurfaceVariant = LightMutedForeground,
    surfaceTint = LightPrimary,
    inverseSurface = LightForeground,
    inverseOnSurface = LightBackground,
    error = LightDestructive,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightDestructive,
    outline = LightInput,
    outlineVariant = LightBorder,
    scrim = Color.Black,
    surfaceBright = LightCard,
    surfaceDim = LightCanvas,
    surfaceContainerLowest = LightCard,
    surfaceContainerLow = LightBackground,
    surfaceContainer = LightPopover,
    surfaceContainerHigh = LightMuted,
    surfaceContainerHighest = LightMuted
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkPrimaryForeground,
    primaryContainer = DarkAccent,
    onPrimaryContainer = DarkAccentForeground,
    inversePrimary = LightPrimary,
    secondary = DarkChart2,
    onSecondary = DarkPrimaryForeground,
    secondaryContainer = DarkSecondary,
    onSecondaryContainer = DarkSecondaryForeground,
    tertiary = DarkChart2,
    onTertiary = DarkPrimaryForeground,
    tertiaryContainer = DarkAccent,
    onTertiaryContainer = DarkAccentForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    surfaceVariant = DarkMuted,
    onSurfaceVariant = DarkMutedForeground,
    surfaceTint = DarkPrimary,
    inverseSurface = DarkForeground,
    inverseOnSurface = DarkBackground,
    error = DarkDestructive,
    onError = DarkPrimaryForeground,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkDestructive,
    outline = DarkInput,
    outlineVariant = DarkBorder,
    scrim = Color.Black,
    surfaceBright = DarkMuted,
    surfaceDim = DarkCanvas,
    surfaceContainerLowest = DarkCanvas,
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkPopover,
    surfaceContainerHigh = DarkMuted,
    surfaceContainerHighest = DarkMuted
)

private val Sans = FontFamily.SansSerif

private val VaultTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    )
)

// --radius is 0.75rem = 12dp. The CSS scale is md 0.8x, lg 1x, xl 1.4x, 2xl 1.8x, 3xl 2.2x.
private val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(9.6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.8.dp),
    large = RoundedCornerShape(21.6.dp),
    extraLarge = RoundedCornerShape(26.4.dp)
)

@Composable
fun VaultLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalVaultExtras provides if (darkTheme) DarkExtras else LightExtras
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = VaultTypography,
            shapes = VaultShapes,
            content = content
        )
    }
}
