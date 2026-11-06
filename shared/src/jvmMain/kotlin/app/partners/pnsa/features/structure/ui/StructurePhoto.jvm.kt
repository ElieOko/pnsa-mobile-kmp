package app.partners.pnsa.features.structure.ui

import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import kotlin.math.max
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

actual class StructurePhotoDisk actual constructor() {
    private val directory = File(System.getProperty("user.home"), ".cache/repere-jeunes/photos").apply { mkdirs() }

    actual fun read(key: String): ByteArray? {
        val file = File(directory, "$key.jpg")
        if (!file.isFile || file.length() <= 0L) return null
        return file.readBytes()
    }

    actual fun write(key: String, bytes: ByteArray) {
        File(directory, "$key.jpg").writeBytes(bytes)
    }
}

internal actual fun decodeStructurePhoto(bytes: ByteArray, maxEdge: Int): DecodedStructurePhoto {
    val encoded = Image.makeFromEncoded(bytes)
    return scaledPhoto(encoded, maxEdge)
}

private fun scaledPhoto(encoded: Image, maxEdge: Int): DecodedStructurePhoto {
    val longest = max(encoded.width, encoded.height).coerceAtLeast(1)
    if (longest <= maxEdge) return encoded.toDecoded()
    val scale = maxEdge.toFloat() / longest
    val width = (encoded.width * scale).toInt().coerceAtLeast(1)
    val height = (encoded.height * scale).toInt().coerceAtLeast(1)
    val surface = Surface.makeRasterN32Premul(width, height)
    surface.canvas.drawImageRect(encoded, Rect.makeWH(width.toFloat(), height.toFloat()))
    return surface.makeImageSnapshot().toDecoded()
}

private fun Image.toDecoded(): DecodedStructurePhoto {
    val data = encodeToData(EncodedImageFormat.JPEG, 80) ?: error("Photo illisible")
    val jpeg = data.bytes
    data.close()
    return DecodedStructurePhoto(toComposeImageBitmap(), jpeg)
}
