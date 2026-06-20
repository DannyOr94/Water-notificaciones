package com.watersf.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ============================================================================
// Water-SF · Tema raíz. Dark-first, anclado en el navy de marca. Envuelve
// toda la app desde MainActivity. [INV-6]
// ============================================================================

private val WaterSfDarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = SurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = OnPrimary,
    tertiary = Tertiary,
    onTertiary = OnPrimary,
    background = BackgroundBase,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    error = SeverityHigh,
    onError = BackgroundBase
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(Dimens.radiusSm),
    medium = RoundedCornerShape(Dimens.radiusMd),
    large = RoundedCornerShape(Dimens.radiusLg)
)

@Composable
fun WaterSfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WaterSfDarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
