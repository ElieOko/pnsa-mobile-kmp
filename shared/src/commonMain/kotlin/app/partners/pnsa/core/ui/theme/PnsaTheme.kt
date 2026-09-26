package app.partners.pnsa.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PnsaBlue = Color(0xFF0069E1)
val PnsaBlueDeep = Color(0xFF0B3C8A)
val PnsaBlueSoft = Color(0xFFE8F2FF)
val PnsaRed = Color(0xFFD01D2A)
val PnsaRedHot = Color(0xFFFF2D55)
val PnsaNavy = Color(0xFF1A2744)
val PnsaInk = Color(0xFF101828)
val PnsaIce = Color(0xFFF3F6FB)
val PnsaChrome = Color(0xFF0A0B10)
val PnsaChromeLift = Color(0xFF161821)
val PnsaMuted = Color(0xFF667085)

val PnsaBlueRed = Brush.linearGradient(listOf(PnsaBlue, Color(0xFF3B82F6), PnsaRed))
val PnsaHeroBrush = Brush.verticalGradient(
    listOf(Color(0xCC0A0B10), Color(0x990B3C8A), Color(0xE60A0B10)),
)

fun motionTween(duration: Int = 420) = tween<Float>(durationMillis = duration)
fun motionSpring() = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

private val LightColors = lightColorScheme(
    primary = PnsaBlue,
    onPrimary = Color.White,
    primaryContainer = PnsaBlueSoft,
    onPrimaryContainer = PnsaNavy,
    secondary = PnsaRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5E8),
    onSecondaryContainer = Color(0xFF4A0B12),
    tertiary = PnsaBlueDeep,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD9E6FF),
    onTertiaryContainer = Color(0xFF0A2048),
    background = PnsaIce,
    onBackground = PnsaInk,
    surface = Color.White,
    onSurface = PnsaInk,
    surfaceVariant = Color(0xFFE6EEF8),
    onSurfaceVariant = Color(0xFF3D4B63),
    outline = Color(0xFFC5D0E0),
    error = PnsaRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5BA8FF),
    onPrimary = Color(0xFF001A3D),
    primaryContainer = Color(0xFF0B3C8A),
    onPrimaryContainer = Color(0xFFD6E7FF),
    secondary = Color(0xFFFF6B7A),
    onSecondary = Color(0xFF3D0610),
    secondaryContainer = Color(0xFF5C1018),
    onSecondaryContainer = Color(0xFFFFD9DD),
    tertiary = Color(0xFF8BB4FF),
    background = Color(0xFF070A12),
    onBackground = Color(0xFFE8EEF8),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE8EEF8),
    surfaceVariant = Color(0xFF1C2436),
    onSurfaceVariant = Color(0xFFB7C3D6),
    outline = Color(0xFF3A4558),
    error = Color(0xFFFF6B7A),
)

private val PnsaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

val LocalEmbeddedChrome = compositionLocalOf { false }
val LocalTabActive = compositionLocalOf { true }

@Composable
fun PnsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = PnsaShapes,
        content = content,
    )
}
