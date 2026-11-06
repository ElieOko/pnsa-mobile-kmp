package app.partners.pnsa.features.structure.ui

import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlin.math.max
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class StructurePhotoDisk actual constructor() {
    private val directory: String? = runCatching {
        val base = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).firstOrNull() as? String
            ?: return@runCatching null
        val path = "$base/structure-photos"
        NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
        path
    }.getOrNull()

    actual fun read(key: String): ByteArray? {
        val path = directory?.let { "$it/$key.jpg" } ?: return null
        val data = NSData.create(contentsOfFile = path) ?: return null
        val size = data.length.toInt()
        if (size <= 0) return null
        val pointer = data.bytes?.reinterpret<ByteVar>() ?: return null
        return pointer.readBytes(size)
    }

    actual fun write(key: String, bytes: ByteArray) {
        val path = directory?.let { "$it/$key.jpg" } ?: return
        bytes.usePinned { pinned ->
            val data = NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            data.writeToFile(path, true)
        }
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
