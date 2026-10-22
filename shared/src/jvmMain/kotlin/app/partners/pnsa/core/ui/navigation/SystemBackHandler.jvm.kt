package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

@Composable
actual fun SystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
}

actual fun exitApplication() {
    exitProcess(0)
}
