package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.core.ui.components.KinshasaGpsMap
import app.partners.pnsa.features.structure.domain.models.HealthStructure

@Composable
actual fun PlatformStructureMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier,
    userLocation: LatLngPoint?,
    route: RouteTrack?,
    followUser: Boolean,
    onFollowInterrupted: () -> Unit,
) {
    KinshasaGpsMap(
        structures = structures,
        selectedId = selectedId,
        onSelect = onSelect,
        modifier = modifier,
        userLat = userLocation?.latitude ?: -4.3276,
        userLon = userLocation?.longitude ?: 15.3136,
        route = route,
    )
}

actual fun usesGoogleMaps(): Boolean = false
