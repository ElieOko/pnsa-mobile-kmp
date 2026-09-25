package app.partners.pnsa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PNSA — Santé des adolescents",
        state = rememberWindowState(size = DpSize(390.dp, 844.dp)),
    ) {
        DesktopPreview()
    }
}

@Composable
private fun DesktopPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B10))
            .clip(RoundedCornerShape(0.dp)),
    ) {
        App()
    }
}
