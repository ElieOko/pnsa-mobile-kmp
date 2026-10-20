package app.partners.pnsa.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.core.ui.theme.PnsaBlue
import app.partners.pnsa.core.ui.theme.PnsaNavy
import app.partners.pnsa.core.ui.theme.PnsaRed
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import app.partners.pnsa.resources.Res
import app.partners.pnsa.resources.map_marker_logo
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.compose.resources.imageResource

object KinshasaMapMath {
    data class Bounds(
        val minLat: Double,
        val maxLat: Double,
        val minLon: Double,
        val maxLon: Double,
    )

    private val defaultBounds = Bounds(
        minLat = -4.50,
        maxLat = -4.29,
        minLon = 15.22,
        maxLon = 15.44,
    )

    fun boundsFor(structures: List<HealthStructure>, userLat: Double, userLon: Double, route: RouteTrack?): Bounds {
        var minLat = Double.POSITIVE_INFINITY
        var maxLat = Double.NEGATIVE_INFINITY
        var minLon = Double.POSITIVE_INFINITY
        var maxLon = Double.NEGATIVE_INFINITY

        fun include(lat: Double, lon: Double) {
            minLat = min(minLat, lat)
            maxLat = max(maxLat, lat)
            minLon = min(minLon, lon)
            maxLon = max(maxLon, lon)
        }

        structures.forEach { structure ->
            val lat = structure.latitude
            val lon = structure.longitude
            if (lat != null && lon != null) include(lat, lon)
        }
        include(userLat, userLon)
        route?.points?.forEach { include(it.latitude, it.longitude) }

        if (!minLat.isFinite() || !maxLat.isFinite() || !minLon.isFinite() || !maxLon.isFinite()) {
            return defaultBounds
        }

        val latSpan = max(maxLat - minLat, 0.08)
        val lonSpan = max(maxLon - minLon, 0.08)
        val latPad = latSpan * 0.12
        val lonPad = lonSpan * 0.12
        return Bounds(
            minLat = (minLat - latPad).coerceIn(-85.0, 85.0),
            maxLat = (maxLat + latPad).coerceIn(-85.0, 85.0),
            minLon = (minLon - lonPad).coerceIn(-180.0, 180.0),
            maxLon = (maxLon + lonPad).coerceIn(-180.0, 180.0),
        )
    }

    fun project(lat: Double, lon: Double, width: Float, height: Float): Offset {
        return project(lat, lon, width, height, defaultBounds)
    }

    fun project(lat: Double, lon: Double, width: Float, height: Float, bounds: Bounds): Offset {
        val lonSpan = (bounds.maxLon - bounds.minLon).takeIf { it > 0.0 } ?: 1.0
        val latSpan = (bounds.maxLat - bounds.minLat).takeIf { it > 0.0 } ?: 1.0
        val x = ((lon - bounds.minLon) / lonSpan).toFloat().coerceIn(0.04f, 0.96f) * width
        val y = ((bounds.maxLat - lat) / latSpan).toFloat().coerceIn(0.06f, 0.94f) * height
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
    satelliteView: Boolean = false,
    userLat: Double = -4.3276,
    userLon: Double = 15.3136,
    route: RouteTrack? = null,
) {
    val markers = remember(structures) { structures.filter { it.hasCoordinates } }
    val selected = markers.firstOrNull { it.id == selectedId }
    val markerLogo = imageResource(Res.drawable.map_marker_logo)
    val projectionBounds = remember(markers, userLat, userLon, route) {
        KinshasaMapMath.boundsFor(markers, userLat, userLon, route)
    }
    val pulse = rememberInfiniteTransition(label = "mapPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

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
                .pointerInput(markers, projectionBounds) {
                    detectTapGestures { tap ->
                        val hit = markers.minByOrNull { item ->
                            val p = KinshasaMapMath.project(item.latitude!!, item.longitude!!, size.width.toFloat(), size.height.toFloat(), projectionBounds)
                            hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble())
                        } ?: return@detectTapGestures
                        val p = KinshasaMapMath.project(hit.latitude!!, hit.longitude!!, size.width.toFloat(), size.height.toFloat(), projectionBounds)
                        if (hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble()) < 48.0) onSelect(hit)
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    if (satelliteView) {
                        listOf(Color(0xFF243828), Color(0xFF335438), Color(0xFF466E42))
                    } else {
                        listOf(Color(0xFF0E1A33), Color(0xFF16325C), Color(0xFF1B4A86))
                    },
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
                val p = KinshasaMapMath.project(commune.lat, commune.lon, w, h, projectionBounds)
                drawCircle(Color.White.copy(alpha = 0.08f), 18f, p)
            }

            route?.points?.zipWithNext { start, end ->
                val from = KinshasaMapMath.project(start.latitude, start.longitude, w, h, projectionBounds)
                val to = KinshasaMapMath.project(end.latitude, end.longitude, w, h, projectionBounds)
                val deepRoute = if (satelliteView) Color(0xFF0D2E0D) else Color(0xFF0B3C8A)
                val routeColor = if (satelliteView) Color(0xFF1AB221) else PnsaBlue
                drawLine(deepRoute, from, to, strokeWidth = 10f, cap = StrokeCap.Round)
                drawLine(routeColor, from, to, strokeWidth = 6f, cap = StrokeCap.Round)
            }

            val user = KinshasaMapMath.project(userLat, userLon, w, h, projectionBounds)
            drawCircle(PnsaBlue.copy(alpha = 0.18f), 30f + pulse.value * 8f, user)
            drawCircle(Color.White, 10f, user)
            drawCircle(if (satelliteView) Color(0xFF1AB221) else PnsaBlue, 6f, user)

            markers.forEach { item ->
                val p = KinshasaMapMath.project(item.latitude!!, item.longitude!!, w, h, projectionBounds)
                val active = item.id == selectedId
                if (active) {
                    drawCircle(PnsaRed.copy(alpha = 0.16f), 21f + pulse.value * 10f, p)
                    drawCircle(PnsaRed.copy(alpha = 0.3f), 24f, p)
                }
                drawCircle(Color.White, if (active) 19f else 17f, p)
                drawCircle(PnsaRed.copy(alpha = 0.12f), if (active) 17f else 15f, p)
                val markerSize = if (active) 24 else 22
                drawImage(
                    image = markerLogo,
                    dstOffset = IntOffset((p.x - markerSize / 2f).toInt(), (p.y - markerSize / 2f).toInt()),
                    dstSize = IntSize(markerSize, markerSize),
                    colorFilter = ColorFilter.tint(PnsaRed),
                )
            }

            drawRoundRect(
                Color(0x990A0B10),
                topLeft = Offset(16f, h - 54f),
                size = Size(210f, 36f),
                cornerRadius = CornerRadius(18f, 18f),
            )
        }

        Text(
            if (satelliteView) "Vue satellite · structures" else "Carte locale · structures",
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

        if (selected != null) {
            GpsPlaceCard(
                item = selected,
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                onOpen = { onSelect(selected) },
            )
        }
    }
}

@Composable
fun GpsPlaceCard(
    item: HealthStructure,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().quietClick(onClick = onOpen),
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
                Text(item.displayAddress, color = PnsaNavy.copy(alpha = 0.58f), fontSize = 11.sp)
                Text(KinshasaMapMath.formatCoord(item.latitude, item.longitude), color = PnsaBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MapLegendRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot(PnsaBlue, "Position")
        LegendDot(PnsaRed, "Structure")
        LegendDot(PnsaRed, "Sélection")
        LegendDot(Color(0xFF0B3C8A), "Itinéraire")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
