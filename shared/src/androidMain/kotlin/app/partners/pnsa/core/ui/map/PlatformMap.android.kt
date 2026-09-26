package app.partners.pnsa.core.ui.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.core.location.bearingDegrees
import app.partners.pnsa.core.location.haversineMeters
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

private class GoogleMapState {
    var lastIds: List<Long?> = emptyList()
    var lastRouteSize: Int = -1
    var lastSelectedId: Long? = null
    var lastUser: LatLngPoint? = null
    var lastFollow = false
    val markers = mutableListOf<Marker>()
    var outline: Polyline? = null
    var track: Polyline? = null
}

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
    val points = remember(structures) { structures.filter { it.hasCoordinates } }
    val state = remember { GoogleMapState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val view = mapView ?: return@DisposableEffect onDispose { }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                Lifecycle.Event.ON_DESTROY -> view.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapsInitializer.initialize(context)
            MapView(context).apply {
                onCreate(Bundle())
                mapView = this
                getMapAsync { googleMap ->
                    configureMap(googleMap)
                    googleMap.setOnCameraMoveStartedListener { reason ->
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            onFollowInterrupted()
                        }
                    }
                    googleMap.setOnMarkerClickListener { marker ->
                        val structure = marker.tag as? HealthStructure ?: return@setOnMarkerClickListener false
                        onSelect(structure)
                        true
                    }
                    bindGoogleMap(state, googleMap, points, selectedId, userLocation, route, followUser)
                }
            }
        },
        update = { view ->
            view.getMapAsync { googleMap ->
                googleMap.setOnCameraMoveStartedListener { reason ->
                    if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                        onFollowInterrupted()
                    }
                }
                googleMap.setOnMarkerClickListener { marker ->
                    val structure = marker.tag as? HealthStructure ?: return@setOnMarkerClickListener false
                    onSelect(structure)
                    true
                }
                bindGoogleMap(state, googleMap, points, selectedId, userLocation, route, followUser)
            }
        },
    )
}

private fun configureMap(map: GoogleMap) {
    map.uiSettings.apply {
        isZoomControlsEnabled = false
        isZoomGesturesEnabled = true
        isScrollGesturesEnabled = true
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = true
        isCompassEnabled = true
        isMyLocationButtonEnabled = false
        isIndoorLevelPickerEnabled = false
        isMapToolbarEnabled = false
    }
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-4.3276, 15.3136), 16.5f))
}

private fun bindGoogleMap(
    state: GoogleMapState,
    map: GoogleMap,
    structures: List<HealthStructure>,
    selectedId: Long?,
    userLocation: LatLngPoint?,
    route: RouteTrack?,
    followUser: Boolean,
) {
    runCatching { map.isMyLocationEnabled = userLocation != null }

    val ids = structures.map { it.id }
    if (ids != state.lastIds) {
        state.lastIds = ids
        state.markers.forEach { it.remove() }
        state.markers.clear()
        structures.forEach { structure ->
            val hue = if (structure.id == selectedId) {
                BitmapDescriptorFactory.HUE_RED
            } else {
                BitmapDescriptorFactory.HUE_AZURE
            }
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(structure.latitude!!, structure.longitude!!))
                    .title(structure.displayName)
                    .snippet(listOfNotNull(structure.displayCity, structure.structureType).joinToString(" · "))
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)),
            )
            marker?.tag = structure
            if (marker != null) state.markers.add(marker)
        }
    } else if (selectedId != state.lastSelectedId) {
        state.markers.forEach { marker ->
            val structure = marker.tag as? HealthStructure
            val hue = if (structure?.id == selectedId) {
                BitmapDescriptorFactory.HUE_RED
            } else {
                BitmapDescriptorFactory.HUE_AZURE
            }
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(hue))
        }
    }

    val routePoints = route?.points.orEmpty()
    if (routePoints.size != state.lastRouteSize) {
        state.lastRouteSize = routePoints.size
        state.outline?.remove()
        state.track?.remove()
        if (routePoints.size >= 2) {
            val latLngs = routePoints.map { LatLng(it.latitude, it.longitude) }
            state.outline = map.addPolyline(
                PolylineOptions().addAll(latLngs).color(0xFF0B3C8A.toInt()).width(18f).geodesic(true),
            )
            state.track = map.addPolyline(
                PolylineOptions().addAll(latLngs).color(0xFF0069E1.toInt()).width(10f).geodesic(true),
            )
        }
    }

    if (followUser && userLocation != null) {
        val moved = state.lastUser == null || haversineMeters(state.lastUser!!, userLocation) >= 1.5
        val resumed = followUser && !state.lastFollow
        if (moved || resumed) {
            val heading = state.lastUser?.let { previous ->
                if (haversineMeters(previous, userLocation) >= 3) bearingDegrees(previous, userLocation) else null
            } ?: map.cameraPosition.bearing
            val zoom = if (resumed || map.cameraPosition.zoom < 15f) 17.2f else map.cameraPosition.zoom.coerceIn(16f, 18.5f)
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(userLocation.latitude, userLocation.longitude))
                        .zoom(zoom)
                        .tilt(42f)
                        .bearing(heading)
                        .build(),
                ),
                380,
                null,
            )
        }
    } else if (selectedId != state.lastSelectedId && !followUser) {
        val selected = structures.firstOrNull { it.id == selectedId }
        if (selected != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.latitude!!, selected.longitude!!), 15.5f),
            )
        }
    }

    state.lastSelectedId = selectedId
    state.lastUser = userLocation
    state.lastFollow = followUser
}

actual fun usesGoogleMaps(): Boolean = true
