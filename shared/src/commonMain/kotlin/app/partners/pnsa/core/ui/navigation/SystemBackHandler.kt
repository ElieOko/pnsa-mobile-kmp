package app.partners.pnsa.core.ui.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)

expect fun exitApplication()
