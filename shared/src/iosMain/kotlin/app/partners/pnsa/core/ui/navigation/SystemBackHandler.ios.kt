package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS uses swipe-back or inline back buttons
}

actual fun exitApplication() {
    // Standard iOS behavior does not programmatically terminate apps
}
