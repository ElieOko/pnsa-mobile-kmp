package app.partners.pnsa.core.ui.map

import android.os.Bundle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.partners.pnsa.core.location.LatLngPoint
import app.partners.pnsa.core.location.RouteTrack
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

private class GoogleMapState {
    var lastIds: List<Long?> = emptyList()
    var lastRouteSize: Int = -1
    var lastSelectedId: Long? = null
    var lastUser: LatLngPoint? = null
    var fittedRoute = false
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
        modifier = modifier.clip(RoundedCornerShape(22.dp)),
        factory = { context ->
            MapsInitializer.initialize(context)
            MapView(context).apply {
                onCreate(Bundle())
                mapView = this
                getMapAsync { googleMap ->
                    configureMap(googleMap)
                    googleMap.setOnMarkerClickListener { marker ->
                        val structure = marker.tag as? HealthStructure ?: return@setOnMarkerClickListener false
                        onSelect(structure)
                        true
                    }
                    bindGoogleMap(state, googleMap, points, selectedId, userLocation, route)
                }
            }
        },
        update = { view ->
            view.getMapAsync { googleMap ->
                googleMap.setOnMarkerClickListener { marker ->
                    val structure = marker.tag as? HealthStructure ?: return@setOnMarkerClickListener false
                    onSelect(structure)
                    true
                }
                bindGoogleMap(state, googleMap, points, selectedId, userLocation, route)
            }
        },
    )
}

private fun configureMap(map: GoogleMap) {
    map.uiSettings.apply {
        isZoomControlsEnabled = true
        isZoomGesturesEnabled = true
        isScrollGesturesEnabled = true
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = true
        isCompassEnabled = true
        isMyLocationButtonEnabled = true
        isMapToolbarEnabled = false
    }
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-4.3276, 15.3136), 12.4f))
}

private fun bindGoogleMap(
    state: GoogleMapState,
    map: GoogleMap,
    structures: List<HealthStructure>,
    selectedId: Long?,
    userLocation: LatLngPoint?,
    route: RouteTrack?,
) {
    runCatching { map.isMyLocationEnabled = userLocation != null }
    map.uiSettings.isMyLocationButtonEnabled = userLocation != null

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
        state.fittedRoute = false
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

    if (routePoints.size >= 2 && !state.fittedRoute) {
        runCatching {
            val bounds = LatLngBounds.builder()
            routePoints.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 96))
            state.fittedRoute = true
        }
    } else if (selectedId != state.lastSelectedId) {
        val selected = structures.firstOrNull { it.id == selectedId }
        if (selected != null && routePoints.size < 2) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.latitude!!, selected.longitude!!), 14.5f),
            )
        }
    } else if (userLocation != null && state.lastUser == null && routePoints.size < 2) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 14.2f),
        )
    }

    state.lastSelectedId = selectedId
    state.lastUser = userLocation
}

actual fun usesGoogleMaps(): Boolean = true
