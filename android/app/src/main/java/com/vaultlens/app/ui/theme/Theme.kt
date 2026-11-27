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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultlens.app.R

/*
 * Design tokens, based on app/globals.css, restyled for the Soft look (navy, pale blue, soft grey-blue).
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
/** Deep navy of the Soft style: hero card and dark highlight surfaces. */
val BrandNavy = Color(0xFF0A3047)

// :root (light)
private val LightCanvas = Color(0xFFE3EAEF)
private val LightBackground = Color(0xFFEDF1F4)
private val LightForeground = Color(0xFF0A3047)
private val LightInk = Color(0xFF0A3047)
private val LightCard = Color(0xFFFFFFFF)
private val LightPopover = Color(0xFFFFFFFF)
private val LightPrimary = Color(0xFF0A3047)
private val LightPrimaryForeground = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFFEEF1F3)
private val LightSecondaryForeground = Color(0xFF0A3047)
private val LightMuted = Color(0xFFEEF1F3)
private val LightMutedForeground = Color(0xFF4F6B7B)
private val LightAccent = Color(0xFFCDEBF6)
private val LightAccentForeground = Color(0xFF0A3047)
private val LightDestructive = Color(0xFFC62F2F)
private val LightBorder = Color(0xFFE4EAEE)
private val LightInput = Color(0xFFD5DEE4)
private val LightChart2 = Color(0xFF6BA8C0)
private val LightErrorContainer = Color(0xFFFBE9E7) // destructive at 12% over card

// .dark
private val DarkCanvas = Color(0xFF04131D)
private val DarkBackground = Color(0xFF081C2A)
private val DarkForeground = Color(0xFFE8F1F6)
private val DarkInk = Color(0xFFCDEBF6)
private val DarkCard = Color(0xFF0F2A3B)
private val DarkPopover = Color(0xFF123247)
private val DarkPrimary = Color(0xFFCDEBF6)
private val DarkPrimaryForeground = Color(0xFF0A3047)
private val DarkSecondary = Color(0xFF163A50)
private val DarkSecondaryForeground = Color(0xFFE8F1F6)
private val DarkMuted = Color(0xFF163A50)
private val DarkMutedForeground = Color(0xFF9DB6C4)
private val DarkAccent = Color(0xFF1B4A63)
private val DarkAccentForeground = Color(0xFFCDEBF6)
private val DarkDestructive = Color(0xFFFF8A80)
private val DarkBorder = Color(0x1AFFFFFF) // white 9% // white 10%
private val DarkInput = Color(0x26FFFFFF) // white 13% // white 15%
private val DarkChart2 = Color(0xFF7CC3DC)
private val DarkErrorContainer = Color(0xFF4A2A2E) // destructive at 20% over card

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

// Outfit (SIL Open Font License, see android/OUTFIT-OFL.txt). Only Regular and Bold are bundled:
// Light and Medium resolve to Regular, SemiBold to Bold.
private val Sans = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

private val VaultTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
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

// Soft style: large rounded corners. Cards use large/extraLarge, chips and inputs medium.
private val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
