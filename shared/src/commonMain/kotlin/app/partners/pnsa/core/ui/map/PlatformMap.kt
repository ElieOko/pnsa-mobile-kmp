package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.features.structure.domain.models.HealthStructure

/** Vues de carte commutables par l'utilisateur (bouton « Vue » sur la carte). */
enum class MapViewStyle(val label: String) {
    Standard("Standard"),
    Satellite("Satellite"),
    Relief("Relief"),
    ;

    fun next(): MapViewStyle = entries[(ordinal + 1) % entries.size]
}

@Composable
expect fun PlatformStructureMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: LatLngPoint? = null,
    route: RouteTrack? = null,
    followUser: Boolean = true,
    onFollowInterrupted: () -> Unit = {},
    viewStyle: MapViewStyle = MapViewStyle.Standard,
)

expect fun usesGoogleMaps(): Boolean
