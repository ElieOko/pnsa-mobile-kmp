package app.partners.pnsa.features.structure.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import app.partners.pnsa.AndroidAppContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

actual class StructurePhotoDisk actual constructor() {
    private val directory: File? = runCatching {
        if (!AndroidAppContext.isBound) return@runCatching null
        File(AndroidAppContext.application.cacheDir, "structure-photos").apply { mkdirs() }
    }.getOrNull()

    actual fun read(key: String): ByteArray? {
        val file = directory?.let { File(it, "$key.jpg") } ?: return null
        if (!file.isFile || file.length() <= 0L) return null
        return file.readBytes()
    }

    actual fun write(key: String, bytes: ByteArray) {
        val folder = directory ?: return
        File(folder, "$key.jpg").writeBytes(bytes)
    }
}

internal actual fun decodeStructurePhoto(bytes: ByteArray, maxEdge: Int): DecodedStructurePhoto {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    val longest = max(bounds.outWidth, bounds.outHeight)
    while (longest / sample > maxEdge && sample < 32) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        ?: error("Photo illisible")
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return DecodedStructurePhoto(bitmap.asImageBitmap(), stream.toByteArray())
}
