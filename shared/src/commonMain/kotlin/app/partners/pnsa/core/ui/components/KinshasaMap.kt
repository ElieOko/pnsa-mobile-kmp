package app.partners.pnsa.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaNavy
import app.partners.pnsa.core.ui.theme.PnsaRed
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import kotlin.math.abs
import kotlin.math.hypot

object KinshasaMapMath {
    const val minLat = -4.50
    const val maxLat = -4.29
    const val minLon = 15.22
    const val maxLon = 15.44

    fun project(lat: Double, lon: Double, width: Float, height: Float): Offset {
        val x = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.04f, 0.96f) * width
        val y = ((maxLat - lat) / (maxLat - minLat)).toFloat().coerceIn(0.06f, 0.94f) * height
        return Offset(x, y)
    }

    fun formatCoord(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return "Coordonnées indisponibles"
        val ns = if (lat < 0) "S" else "N"
        val ew = if (lon < 0) "O" else "E"
        return "${abs(lat).format6()}° $ns  ·  ${abs(lon).format6()}° $ew"
    }

    private fun Double.format6(): String {
        val scaled = kotlin.math.round(this * 1_000_000.0) / 1_000_000.0
        return scaled.toString()
    }
}

private data class CommuneLabel(val name: String, val lat: Double, val lon: Double)

@Composable
fun KinshasaGpsMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier = Modifier,
    userLat: Double = -4.3276,
    userLon: Double = 15.3136,
) {
    val pulse by rememberInfiniteTransition(label = "gps-pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "pulse",
    )
    val markers = remember(structures) { structures.filter { it.hasCoordinates } }
    val selected = markers.firstOrNull { it.id == selectedId }

    val communes = remember {
        listOf(
            CommuneLabel("Gombe", -4.305, 15.313),
            CommuneLabel("Ngaliema", -4.330, 15.255),
            CommuneLabel("Kintambo", -4.328, 15.278),
            CommuneLabel("Barumbu", -4.318, 15.330),
            CommuneLabel("Limete", -4.375, 15.345),
            CommuneLabel("Lemba", -4.422, 15.310),
            CommuneLabel("Masina", -4.385, 15.365),
            CommuneLabel("Ndjili", -4.408, 15.378),
            CommuneLabel("Kimbanseke", -4.400, 15.410),
            CommuneLabel("Mont Ngafula", -4.455, 15.270),
        )
    }

    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .shadow(8.dp, RoundedCornerShape(28.dp)),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(markers) {
                    detectTapGestures { tap ->
                        val hit = markers.minByOrNull { item ->
                            val p = KinshasaMapMath.project(item.latitude!!, item.longitude!!, size.width.toFloat(), size.height.toFloat())
                            hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble())
                        } ?: return@detectTapGestures
                        val p = KinshasaMapMath.project(hit.latitude!!, hit.longitude!!, size.width.toFloat(), size.height.toFloat())
                        if (hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble()) < 48.0) onSelect(hit)
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0E1A33), Color(0xFF16325C), Color(0xFF1B4A86)),
                ),
            )
            val river = Path().apply {
                moveTo(0f, h * 0.04f)
                cubicTo(w * 0.22f, h * 0.02f, w * 0.38f, h * 0.12f, w * 0.55f, h * 0.09f)
                cubicTo(w * 0.78f, h * 0.05f, w * 0.9f, h * 0.16f, w, h * 0.12f)
                lineTo(w, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(river, Color(0xFF3BA3FF).copy(alpha = 0.55f))
            drawPath(river, Color(0xFF9AD4FF).copy(alpha = 0.35f), style = Stroke(width = 10f))

            for (i in 1..8) {
                val y = h * i / 9f
                drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                val x = w * i / 9f
                drawLine(Color.White.copy(alpha = 0.05f), Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
            }

            val boulevard = Path().apply {
                moveTo(w * 0.18f, h * 0.22f)
                cubicTo(w * 0.32f, h * 0.38f, w * 0.48f, h * 0.42f, w * 0.72f, h * 0.58f)
                cubicTo(w * 0.82f, h * 0.66f, w * 0.88f, h * 0.78f, w * 0.92f, h * 0.9f)
            }
            drawPath(boulevard, Color(0x66FFFFFF), style = Stroke(width = 7f, cap = StrokeCap.Round))
            val kasavubu = Path().apply {
                moveTo(w * 0.08f, h * 0.36f)
                cubicTo(w * 0.28f, h * 0.4f, w * 0.46f, h * 0.55f, w * 0.62f, h * 0.82f)
            }
            drawPath(kasavubu, Color(0x44FFFFFF), style = Stroke(width = 5f, cap = StrokeCap.Round))

            communes.forEach { commune ->
                val p = KinshasaMapMath.project(commune.lat, commune.lon, w, h)
                drawCircle(Color.White.copy(alpha = 0.08f), 18f, p)
            }

            val user = KinshasaMapMath.project(userLat, userLon, w, h)
            drawCircle(PnsaBlue.copy(alpha = 0.18f * pulse), 34f * pulse, user)
            drawCircle(Color.White, 10f, user)
            drawCircle(PnsaBlue, 6f, user)

            markers.forEach { item ->
                val p = KinshasaMapMath.project(item.latitude!!, item.longitude!!, w, h)
                val active = item.id == selectedId
                val pin = if (active) PnsaRed else Color(0xFFFFC107)
                val halo = if (active) PnsaRed.copy(alpha = 0.28f) else PnsaBlue.copy(alpha = 0.22f)
                drawCircle(halo, if (active) 22f else 14f, p)
                val path = Path().apply {
                    moveTo(p.x, p.y + 16f)
                    quadraticTo(p.x - 14f, p.y - 2f, p.x, p.y - 18f)
                    quadraticTo(p.x + 14f, p.y - 2f, p.x, p.y + 16f)
                    close()
                }
                drawPath(path, pin)
                drawCircle(Color.White, 4.5f, Offset(p.x, p.y - 8f))
            }

            drawRoundRect(
                Color(0x990A0B10),
                topLeft = Offset(16f, h - 54f),
                size = Size(210f, 36f),
                cornerRadius = CornerRadius(18f, 18f),
            )
        }

        Text(
            "Carte locale · Kinshasa",
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
        )
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.92f),
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "Position indicative",
                tint = PnsaBlue,
                modifier = Modifier.padding(8.dp).size(18.dp),
            )
        }

        AnimatedVisibility(
            visible = selected != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
        ) {
            selected?.let { item ->
                GpsPlaceCard(item, onOpen = { onSelect(item) })
            }
        }
    }
}

@Composable
fun GpsPlaceCard(item: HealthStructure, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PnsaRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = PnsaRed)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PnsaNavy)
                Text("${item.displayCity} · ${item.structureType ?: "Centre SSR"}", color = PnsaNavy.copy(alpha = 0.65f), fontSize = 12.sp)
                Text(KinshasaMapMath.formatCoord(item.latitude, item.longitude), color = PnsaBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MapLegendRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot(PnsaBlue, "Position")
        LegendDot(Color(0xFFFFC107), "Centre")
        LegendDot(PnsaRed, "Sélection")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
