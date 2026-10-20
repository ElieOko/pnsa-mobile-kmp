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

val wsColor = Color.Green.copy(0.3f)
val bagdeColor = Color(0xFF1AB221)
val badgeColor = bagdeColor

val PnsaGreenDeep = Color(0xFF0E7A14)
val PnsaGreenSoft = Color(0xFFE8F8E9)
val PnsaGreenBubbleSent = Color(0xFFDCF8C6)
val PnsaChatCanvas = Color(0xFFF5F8F5)

val PnsaBlue = bagdeColor
val PnsaBlueDeep = PnsaGreenDeep
val PnsaBlueSoft = PnsaGreenSoft
val PnsaRed = Color(0xFF138A19)
val PnsaRedHot = bagdeColor
val PnsaNavy = Color(0xFF0D1F10)
val PnsaInk = Color(0xFF111B13)
val PnsaIce = Color(0xFFFFFFFF)
val PnsaChrome = Color.White
val PnsaChromeLift = Color(0xFFF6FBF6)
val PnsaMuted = Color(0xFF5C6B5E)

val PnsaBlueRed = Brush.linearGradient(listOf(bagdeColor, Color(0xFF28C930), PnsaGreenDeep))
val PnsaHeroBrush = Brush.verticalGradient(
    listOf(Color(0xCC0D1F10), Color(0x990E7A14), Color(0xE60D1F10)),
)

fun motionTween(duration: Int = 420) = tween<Float>(durationMillis = duration)
fun motionSpring() = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

private val LightColors = lightColorScheme(
    primary = bagdeColor,
    onPrimary = Color.White,
    primaryContainer = PnsaGreenSoft,
    onPrimaryContainer = PnsaNavy,
    secondary = PnsaGreenDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2F5E3),
    onSecondaryContainer = Color(0xFF0A2E0D),
    tertiary = PnsaGreenDeep,
    onTertiary = Color.White,
    tertiaryContainer = PnsaGreenSoft,
    onTertiaryContainer = Color(0xFF0A2E0D),
    background = Color.White,
    onBackground = PnsaInk,
    surface = Color.White,
    onSurface = PnsaInk,
    surfaceVariant = Color(0xFFF1F7F1),
    onSurfaceVariant = Color(0xFF3E4E40),
    outline = Color(0xFFD5E4D6),
    error = Color(0xFFD32F2F),
)

private val DarkColors = darkColorScheme(
    primary = bagdeColor,
    onPrimary = Color.White,
    primaryContainer = PnsaGreenDeep,
    onPrimaryContainer = PnsaGreenSoft,
    secondary = Color(0xFF4CD953),
    onSecondary = Color(0xFF062408),
    secondaryContainer = Color(0xFF134E17),
    onSecondaryContainer = Color(0xFFD7FAD9),
    tertiary = Color(0xFF7BE881),
    background = Color(0xFF0B140C),
    onBackground = Color(0xFFEAF4EB),
    surface = Color(0xFF122014),
    onSurface = Color(0xFFEAF4EB),
    surfaceVariant = Color(0xFF1A2D1C),
    onSurfaceVariant = Color(0xFFB7C9B9),
    outline = Color(0xFF354A37),
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
