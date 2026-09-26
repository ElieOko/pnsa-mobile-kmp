package app.partners.pnsa.core.location

import app.partners.pnsa.core.config.AppConfig
import app.partners.pnsa.core.network.AppJson
import app.partners.pnsa.core.network.platformHttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DirectionsClient {
    private val http = platformHttpClient {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    suspend fun route(origin: LatLngPoint, destination: LatLngPoint): RouteTrack? {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving&language=fr&key=${AppConfig.googleMapsApiKey}"
        val body = runCatching { http.get(url).bodyAsText() }.getOrNull() ?: return fallback(origin, destination)
        val parsed = runCatching { AppJson.decodeFromString(DirectionsResponse.serializer(), body) }.getOrNull()
        val route = parsed?.routes?.firstOrNull()
        val points = route?.overviewPolyline?.points?.let(::decodePolyline).orEmpty()
        if (points.size >= 2 && parsed?.status == "OK") {
            val leg = route?.legs?.firstOrNull()
            return RouteTrack(
                points = points,
                distanceText = leg?.distance?.text,
                durationText = leg?.duration?.text,
            )
        }
        return fallback(origin, destination)
    }

    private fun fallback(origin: LatLngPoint, destination: LatLngPoint): RouteTrack =
        RouteTrack(points = listOf(origin, destination))
}

@Serializable
private data class DirectionsResponse(
    val status: String? = null,
    val routes: List<DirectionsRouteDto> = emptyList(),
)

@Serializable
private data class DirectionsRouteDto(
    @SerialName("overview_polyline") val overviewPolyline: EncodedPolylineDto? = null,
    val legs: List<DirectionsLegDto> = emptyList(),
)

@Serializable
private data class EncodedPolylineDto(
    val points: String? = null,
)

@Serializable
private data class DirectionsLegDto(
    val distance: TextValueDto? = null,
    val duration: TextValueDto? = null,
)

@Serializable
private data class TextValueDto(
    val text: String? = null,
    val value: Long? = null,
)
