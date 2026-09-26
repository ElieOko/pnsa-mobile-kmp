package app.partners.pnsa.core.location

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
expect fun rememberLocationGranted(): Boolean

expect fun locationUpdates(): Flow<LatLngPoint>
