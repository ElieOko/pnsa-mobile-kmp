package app.partners.pnsa.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

private fun CLAuthorizationStatus.isGranted(): Boolean =
    this == kCLAuthorizationStatusAuthorizedWhenInUse || this == kCLAuthorizationStatusAuthorizedAlways

@OptIn(ExperimentalForeignApi::class)
private class IosLocationDelegate(
    val onAuthorized: (Boolean) -> Unit,
    val onLocation: (LatLngPoint) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorized(manager.authorizationStatus.isGranted())
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        val point = location.coordinate.useContents {
            LatLngPoint(latitude, longitude)
        }
        onLocation(point)
    }
}

@Composable
actual fun rememberLocationGranted(): Boolean {
    var granted by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val manager = CLLocationManager()
        val delegate = IosLocationDelegate(
            onAuthorized = { granted = it },
            onLocation = {},
        )
        manager.delegate = delegate
        val status = manager.authorizationStatus
        if (status == kCLAuthorizationStatusNotDetermined) {
            manager.requestWhenInUseAuthorization()
        } else {
            granted = status.isGranted()
        }
        onDispose { manager.delegate = null }
    }
    return granted
}

@OptIn(ExperimentalForeignApi::class)
actual fun locationUpdates(): Flow<LatLngPoint> = callbackFlow {
    val manager = CLLocationManager()
    val delegate = IosLocationDelegate(
        onAuthorized = { authorized ->
            if (authorized) manager.startUpdatingLocation()
        },
        onLocation = { trySend(it) },
    )
    manager.delegate = delegate
    manager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
    if (manager.authorizationStatus.isGranted()) {
        manager.startUpdatingLocation()
    } else if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
        manager.requestWhenInUseAuthorization()
    }
    awaitClose {
        manager.stopUpdatingLocation()
        manager.delegate = null
    }
}
