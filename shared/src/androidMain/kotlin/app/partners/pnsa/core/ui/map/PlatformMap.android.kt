package app.partners.pnsa.core.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.MotionEvent
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
import app.partners.pnsa.core.location.haversineMeters
import app.partners.pnsa.core.ui.theme.LocalTabActive
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import app.partners.pnsa.shared.R

private class GoogleMapState {
    var map: GoogleMap? = null
    var lastIds: List<Long?> = emptyList()
    var lastRouteSize: Int = -1
    var lastSelectedId: Long? = null
    var lastUser: LatLngPoint? = null
    var lastFollow = false
    var lastMapStyle: MapViewStyle? = null
    val markers = mutableListOf<Marker>()
    var track: Polyline? = null
    var markerIcon: BitmapDescriptor? = null
    var selectedMarkerIcon: BitmapDescriptor? = null
}

@SuppressLint("ClickableViewAccessibility")
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
    mapStyle: MapViewStyle,
) {
    val points = remember(structures) { structures.filter { it.hasCoordinates } }
    val state = remember { GoogleMapState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val tabActive = LocalTabActive.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner, mapView, tabActive) {
        val view = mapView ?: return@DisposableEffect onDispose { }
        runCatching {
            if (tabActive) {
                view.onStart()
                view.onResume()
            } else {
                view.onPause()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (!tabActive) return@LifecycleEventObserver
            runCatching {
                when (event) {
                    Lifecycle.Event.ON_START -> view.onStart()
                    Lifecycle.Event.ON_RESUME -> view.onResume()
                    Lifecycle.Event.ON_PAUSE -> view.onPause()
                    Lifecycle.Event.ON_STOP -> view.onStop()
                    Lifecycle.Event.ON_DESTROY -> view.onDestroy()
                    else -> Unit
                }
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
                setOnTouchListener { touched, event ->
                    touched.parent?.requestDisallowInterceptTouchEvent(true)
                    if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                        touched.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                getMapAsync { googleMap ->
                    state.map = googleMap
                    if (state.markerIcon == null) {
                        state.markerIcon = runCatching { createMarkerIcon(context, selected = false) }.getOrNull()
                    }
                    if (state.selectedMarkerIcon == null) {
                        state.selectedMarkerIcon = runCatching { createMarkerIcon(context, selected = true) }.getOrNull()
                    }
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
                    bindGoogleMap(state, googleMap, points, selectedId, userLocation, route, followUser, mapStyle)
                }
            }
        },
        update = {
            state.map?.let { googleMap ->
                bindGoogleMap(state, googleMap, points, selectedId, userLocation, route, followUser, mapStyle)
            }
        },
    )
}

private fun configureMap(map: GoogleMap) {
    map.mapType = GoogleMap.MAP_TYPE_NORMAL
    map.isBuildingsEnabled = false
    map.isIndoorEnabled = false
    map.isTrafficEnabled = false
    map.uiSettings.apply {
        isZoomControlsEnabled = false
        isZoomGesturesEnabled = true
        isScrollGesturesEnabled = true
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = false
        isCompassEnabled = true
        isMyLocationButtonEnabled = false
        isIndoorLevelPickerEnabled = false
        isMapToolbarEnabled = false
    }
    map.setMinZoomPreference(11f)
    map.setMaxZoomPreference(18f)
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-4.3276, 15.3136), 15.8f))
}

private fun bindGoogleMap(
    state: GoogleMapState,
    map: GoogleMap,
    structures: List<HealthStructure>,
    selectedId: Long?,
    userLocation: LatLngPoint?,
    route: RouteTrack?,
    followUser: Boolean,
    mapStyle: MapViewStyle,
) {
    runCatching { map.isMyLocationEnabled = userLocation != null }

    if (state.lastMapStyle != mapStyle) {
        state.lastMapStyle = mapStyle
        map.mapType = when (mapStyle) {
            MapViewStyle.Standard -> GoogleMap.MAP_TYPE_NORMAL
            MapViewStyle.Satellite -> GoogleMap.MAP_TYPE_HYBRID
            MapViewStyle.Terrain -> GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    val ids = structures.map { it.id }
    if (ids != state.lastIds) {
        state.lastIds = ids
        state.markers.forEach { it.remove() }
        state.markers.clear()
        structures.forEach { structure ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(structure.latitude!!, structure.longitude!!))
                    .title(structure.displayName)
                    .snippet(listOfNotNull(structure.displayCity, structure.structureType).joinToString(" · "))
                    .icon(markerIconFor(state, structure.id == selectedId)),
            )
            marker?.tag = structure
            if (marker != null) state.markers.add(marker)
        }
    } else if (selectedId != state.lastSelectedId) {
        state.markers.forEach { marker ->
            val structure = marker.tag as? HealthStructure
            marker.setIcon(markerIconFor(state, structure?.id == selectedId))
        }
    }

    val routePoints = route?.points.orEmpty()
    if (routePoints.size != state.lastRouteSize) {
        state.lastRouteSize = routePoints.size
        state.track?.remove()
        if (routePoints.size >= 2) {
            state.track = map.addPolyline(
                PolylineOptions()
                    .addAll(routePoints.map { LatLng(it.latitude, it.longitude) })
                    .color(0xFF1AB221.toInt())
                    .width(10f)
                    .geodesic(false),
            )
        }
    }

    if (followUser && userLocation != null) {
        val moved = state.lastUser == null || haversineMeters(state.lastUser!!, userLocation) >= 8
        val resumed = followUser && !state.lastFollow
        if (moved || resumed) {
            val target = LatLng(userLocation.latitude, userLocation.longitude)
            if (resumed) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16.2f))
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLng(target))
            }
        }
    } else if (selectedId != state.lastSelectedId) {
        val selected = structures.firstOrNull { it.id == selectedId }
        if (selected != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.latitude!!, selected.longitude!!), 15.4f),
            )
        }
    }

    state.lastSelectedId = selectedId
    state.lastUser = userLocation
    state.lastFollow = followUser
}

private fun markerIconFor(state: GoogleMapState, selected: Boolean): BitmapDescriptor =
    when {
        selected -> state.selectedMarkerIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        else -> state.markerIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    }

private fun createMarkerIcon(context: Context, selected: Boolean): BitmapDescriptor {
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.map_marker_logo)
        ?: return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    val size = 96
    val marker = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(marker)
    val center = size / 2f
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (selected) Color.parseColor("#1AB221") else Color.parseColor("#0F7E15")
        alpha = if (selected) 255 else 225
    }
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    canvas.drawCircle(center, center, 34f, outerPaint)
    canvas.drawCircle(center, center, 30f, innerPaint)

    val iconSize = if (selected) 40 else 36
    val left = ((size - iconSize) / 2f)
    val top = ((size - iconSize) / 2f)
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = PorterDuffColorFilter(Color.parseColor("#1AB221"), PorterDuff.Mode.SRC_IN)
    }
    val scaledLogo = Bitmap.createScaledBitmap(logo, iconSize, iconSize, true)
    canvas.drawBitmap(scaledLogo, left, top, iconPaint)
    return BitmapDescriptorFactory.fromBitmap(marker)
}

actual fun usesGoogleMaps(): Boolean = true
