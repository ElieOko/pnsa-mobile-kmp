package app.partners.pnsa.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
actual fun rememberLocationGranted(): Boolean {
    LaunchedEffect(Unit) { }
    return false
}

actual fun locationUpdates(): Flow<LatLngPoint> = emptyFlow()
