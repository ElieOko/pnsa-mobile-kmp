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

/** Couleurs de référence (style WhatsApp) : vert badge, vert doux et blanc. */
val BadgeColor = Color(0xFF1AB221)
val WsColor = Color.Green.copy(alpha = 0.3f)
val WsGreenDark = Color(0xFF0E7A16)
val WsGreenDeep = Color(0xFF0A5C10)
val WsBubbleSent = Color(0xFFD9F8CB)
val WsChatBackground = Color(0xFFF2F7F0)
val WsSurfaceSoft = Color(0xFFEDF7EC)

val PnsaBlue = BadgeColor
val PnsaBlueDeep = WsGreenDeep
val PnsaBlueSoft = WsSurfaceSoft
val PnsaRed = Color(0xFFD01D2A)
val PnsaRedHot = BadgeColor
val PnsaNavy = Color(0xFF12240F)
val PnsaInk = Color(0xFF111B0E)
val PnsaIce = Color(0xFFF7FAF5)
val PnsaChrome = Color.White
val PnsaChromeLift = Color.White
val PnsaMuted = Color(0xFF667085)

val PnsaBlueRed = Brush.linearGradient(listOf(WsGreenDark, BadgeColor, WsColor))
val PnsaHeroBrush = Brush.verticalGradient(
    listOf(Color(0xCC0A5C10), Color(0x991AB221), Color(0xE60A5C10)),
)

fun motionTween(duration: Int = 420) = tween<Float>(durationMillis = duration)
fun motionSpring() = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

private val LightColors = lightColorScheme(
    primary = BadgeColor,
    onPrimary = Color.White,
    primaryContainer = WsColor,
    onPrimaryContainer = WsGreenDeep,
    secondary = BadgeColor,
    onSecondary = Color.White,
    secondaryContainer = WsSurfaceSoft,
    onSecondaryContainer = WsGreenDeep,
    tertiary = WsGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = WsColor,
    onTertiaryContainer = WsGreenDeep,
    background = Color.White,
    onBackground = PnsaInk,
    surface = Color.White,
    onSurface = PnsaInk,
    surfaceVariant = WsSurfaceSoft,
    onSurfaceVariant = Color(0xFF3D4B3A),
    outline = Color(0xFFD5E8D2),
    error = PnsaRed,
)

private val DarkColors = darkColorScheme(
    primary = BadgeColor,
    onPrimary = Color.White,
    primaryContainer = WsGreenDeep,
    onPrimaryContainer = Color(0xFFD9F8CB),
    secondary = BadgeColor,
    onSecondary = Color.White,
    secondaryContainer = WsGreenDeep,
    onSecondaryContainer = Color(0xFFD9F8CB),
    tertiary = WsColor,
    background = Color(0xFF0B120A),
    onBackground = Color(0xFFEAF5E8),
    surface = Color(0xFF14200F),
    onSurface = Color(0xFFEAF5E8),
    surfaceVariant = Color(0xFF1C2B18),
    onSurfaceVariant = Color(0xFFB7C9B4),
    outline = Color(0xFF355232),
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
