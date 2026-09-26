package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.features.structure.domain.models.HealthStructure

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
)

expect fun usesGoogleMaps(): Boolean
