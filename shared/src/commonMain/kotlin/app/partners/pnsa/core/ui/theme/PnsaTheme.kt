package app.partners.pnsa.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PnsaGreen = Color(0xFF0B6B45)
val PnsaGreenSoft = Color(0xFF1B8F5E)
val PnsaMint = Color(0xFFE4F4EC)
val PnsaAmber = Color(0xFFE67E22)
val PnsaSky = Color(0xFF167A8B)
val PnsaCream = Color(0xFFF6F8F4)
val PnsaInk = Color(0xFF12281E)
val PnsaSand = Color(0xFFFFF4E8)

private val LightColors = lightColorScheme(
    primary = PnsaGreen,
    onPrimary = Color.White,
    primaryContainer = PnsaMint,
    onPrimaryContainer = PnsaInk,
    secondary = Color(0xFFD96B16),
    onSecondary = Color.White,
    secondaryContainer = PnsaSand,
    onSecondaryContainer = Color(0xFF4A2A0A),
    tertiary = PnsaSky,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7F1F5),
    onTertiaryContainer = Color(0xFF08343A),
    background = PnsaCream,
    onBackground = PnsaInk,
    surface = Color.White,
    onSurface = PnsaInk,
    surfaceVariant = Color(0xFFE7EEE8),
    onSurfaceVariant = Color(0xFF3D5348),
    outline = Color(0xFFB7C6BB),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7ED9A8),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF0B6B45),
    onPrimaryContainer = Color(0xFFD4F5E4),
    secondary = Color(0xFFFFB677),
    onSecondary = Color(0xFF4A2A0A),
    background = Color(0xFF0E1A14),
    onBackground = Color(0xFFE6F2EA),
    surface = Color(0xFF15241C),
    onSurface = Color(0xFFE6F2EA),
    surfaceVariant = Color(0xFF24362D),
    onSurfaceVariant = Color(0xFFC5D4CB),
)

private val PnsaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

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
