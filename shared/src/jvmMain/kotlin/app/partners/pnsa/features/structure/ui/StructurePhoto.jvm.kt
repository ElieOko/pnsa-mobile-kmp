package app.partners.pnsa.features.structure.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlin.math.max
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

actual fun decodeStructurePhoto(bytes: ByteArray, maxEdge: Int): ImageBitmap {
    val encoded = Image.makeFromEncoded(bytes)
    val longest = max(encoded.width, encoded.height).coerceAtLeast(1)
    if (longest <= maxEdge) return encoded.toComposeImageBitmap()
    val scale = maxEdge.toFloat() / longest
    val width = (encoded.width * scale).toInt().coerceAtLeast(1)
    val height = (encoded.height * scale).toInt().coerceAtLeast(1)
    val surface = Surface.makeRasterN32Premul(width, height)
    surface.canvas.drawImageRect(encoded, Rect.makeWH(width.toFloat(), height.toFloat()))
    return surface.makeImageSnapshot().toComposeImageBitmap()
}
