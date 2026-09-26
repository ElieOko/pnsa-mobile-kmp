package app.partners.pnsa.core.location

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double,
)

data class RouteTrack(
    val points: List<LatLngPoint>,
    val distanceText: String? = null,
    val durationText: String? = null,
) {
    val summary: String
        get() = listOfNotNull(durationText, distanceText).joinToString(" · ").ifBlank { "Itinéraire" }
}

fun haversineMeters(from: LatLngPoint, to: LatLngPoint): Double {
    val earth = 6_371_000.0
    val dLat = toRadians(to.latitude - from.latitude)
    val dLon = toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(toRadians(from.latitude)) * cos(toRadians(to.latitude)) *
        sin(dLon / 2) * sin(dLon / 2)
    return 2 * earth * atan2(sqrt(a), sqrt(1 - a))
}

fun shouldRefreshRoute(previous: LatLngPoint?, next: LatLngPoint, minMeters: Double = 80.0): Boolean {
    if (previous == null) return true
    return haversineMeters(previous, next) >= minMeters
}

fun decodePolyline(encoded: String): List<LatLngPoint> {
    val points = ArrayList<LatLngPoint>()
    var index = 0
    var lat = 0
    var lng = 0
    while (index < encoded.length) {
        var result = 0
        var shift = 0
        var byte: Int
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20 && index < encoded.length)
        val deltaLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += deltaLat
        result = 0
        shift = 0
        if (index >= encoded.length) break
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20 && index < encoded.length)
        val deltaLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += deltaLng
        points.add(LatLngPoint(lat / 1e5, lng / 1e5))
    }
    return points
}

fun bearingDegrees(from: LatLngPoint, to: LatLngPoint): Float {
    val lat1 = toRadians(from.latitude)
    val lat2 = toRadians(to.latitude)
    val dLon = toRadians(to.longitude - from.longitude)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearing = atan2(y, x) * 180.0 / PI
    return ((bearing + 360.0) % 360.0).toFloat()
}

private fun toRadians(degrees: Double): Double = degrees * PI / 180.0
