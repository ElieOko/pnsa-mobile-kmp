package app.partners.pnsa.core.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

private var currentAndroidActivity: Activity? = null

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun SystemBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    DisposableEffect(context) {
        currentAndroidActivity = context.findActivity()
        onDispose {
            if (currentAndroidActivity === context.findActivity()) {
                currentAndroidActivity = null
            }
        }
    }
    BackHandler(enabled = enabled, onBack = onBack)
}

actual fun exitApplication() {
    val act = currentAndroidActivity
    if (act != null) {
        act.finishAffinity()
    } else {
        exitProcess(0)
    }
}
