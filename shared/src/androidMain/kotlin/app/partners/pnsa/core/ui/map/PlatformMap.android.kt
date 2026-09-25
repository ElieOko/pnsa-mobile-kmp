package app.partners.pnsa.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private class OsmMapState {
    var lastIds: List<Long?> = emptyList()
    var lastSelectedId: Long? = null
}

@Composable
actual fun PlatformStructureMap(
    structures: List<HealthStructure>,
    selectedId: Long?,
    onSelect: (HealthStructure) -> Unit,
    modifier: Modifier,
) {
    val markers = remember(structures) { structures.filter { it.hasCoordinates } }
    val state = remember { OsmMapState() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                controller.setZoom(12.2)
                controller.setCenter(GeoPoint(-4.3276, 15.3136))
                minZoomLevel = 10.0
                maxZoomLevel = 18.0
                onResume()
            }
        },
        update = { map ->
            val ids = markers.map { it.id }
            if (ids != state.lastIds) {
                state.lastIds = ids
                map.overlays.removeAll { it is Marker }
                markers.forEach { structure ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(structure.latitude!!, structure.longitude!!)
                        title = structure.displayName
                        snippet = listOfNotNull(structure.displayCity, structure.structureType).joinToString(" · ")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        relatedObject = structure
                        setOnMarkerClickListener { _, _ ->
                            onSelect(structure)
                            true
                        }
                    }
                    map.overlays.add(marker)
                }
                map.invalidate()
            }
            if (selectedId != state.lastSelectedId) {
                state.lastSelectedId = selectedId
                val selected = markers.firstOrNull { it.id == selectedId }
                if (selected != null) {
                    map.controller.setCenter(GeoPoint(selected.latitude!!, selected.longitude!!))
                }
            }
        },
        onRelease = { map ->
            map.onPause()
            map.onDetach()
        },
    )
}

actual fun usesOpenStreetMap(): Boolean = true
