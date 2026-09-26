package app.partners.pnsa.core.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.partners.pnsa.AndroidAppContext
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun hasLocationPermission(): Boolean {
    if (!AndroidAppContext.isBound) return false
    val context = AndroidAppContext.application
    return locationPermissions.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
actual fun rememberLocationGranted(): Boolean {
    val context = LocalContext.current
    fun check(): Boolean = locationPermissions.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    var granted by remember { mutableStateOf(check()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        granted = check()
    }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(locationPermissions)
    }
    return granted
}

actual fun locationUpdates(): Flow<LatLngPoint> = callbackFlow {
    if (!hasLocationPermission()) {
        close()
        return@callbackFlow
    }
    val client = LocationServices.getFusedLocationProviderClient(AndroidAppContext.application)
    runCatching {
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                trySend(LatLngPoint(location.latitude, location.longitude))
            }
        }
    }
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_200L)
        .setMinUpdateIntervalMillis(600L)
        .setMinUpdateDistanceMeters(2f)
        .setWaitForAccurateLocation(false)
        .build()
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            trySend(LatLngPoint(location.latitude, location.longitude))
        }
    }
    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    awaitClose { client.removeLocationUpdates(callback) }
}
