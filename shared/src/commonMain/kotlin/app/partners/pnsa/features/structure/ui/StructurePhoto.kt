package app.partners.pnsa.features.structure.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import app.partners.pnsa.core.network.platformHttpClient
import app.partners.pnsa.core.ui.theme.RepereLavender
import app.partners.pnsa.features.structure.domain.models.HealthStructure
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val PHOTO_MAX_EDGE = 720
private const val PHOTO_CACHE_SIZE = 64
private const val PHOTO_PARALLEL = 3

private val photoClient = platformHttpClient {
    expectSuccess = false
    followRedirects = true
    install(HttpTimeout) {
        requestTimeoutMillis = 25_000
        connectTimeoutMillis = 12_000
    }
}

private val photoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
private val photoGate = Mutex()
private val photoQueue = ArrayDeque<String>()
private val photoInflight = mutableSetOf<String>()
private val photoFailed = mutableSetOf<String>()
private val photoMemory = mutableSetOf<String>()
private var photoWorkers = 0
private val photoDisk = StructurePhotoDisk()

internal val structurePhotoBitmaps = mutableStateMapOf<String, ImageBitmap>()

internal class DecodedStructurePhoto(val bitmap: ImageBitmap, val jpeg: ByteArray)

internal expect class StructurePhotoDisk() {
    fun read(key: String): ByteArray?
    fun write(key: String, bytes: ByteArray)
}

internal expect fun decodeStructurePhoto(bytes: ByteArray, maxEdge: Int): DecodedStructurePhoto

fun prefetchStructurePhotos(urls: List<String>, ahead: Boolean = true) {
    val wanted = urls.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    if (wanted.isEmpty()) return
    photoScope.launch {
        photoGate.withLock {
            val ordered = if (ahead) wanted.asReversed() else wanted
            for (url in ordered) {
                if (ahead) photoFailed.remove(url)
                if (url in photoMemory || url in photoInflight || url in photoFailed) continue
                photoQueue.remove(url)
                if (ahead) photoQueue.addFirst(url) else photoQueue.addLast(url)
            }
            while (photoWorkers < PHOTO_PARALLEL && photoQueue.isNotEmpty()) {
                photoWorkers++
                launchPhotoWorker()
            }
        }
    }
}

private fun launchPhotoWorker() {
    photoScope.launch {
        while (true) {
            val url = photoGate.withLock {
                val next = photoQueue.removeFirstOrNull()
                if (next == null) {
                    photoWorkers--
                    null
                } else {
                    photoInflight += next
                    next
                }
            } ?: return@launch
            val loaded = runCatching { loadStructurePhoto(url) }.getOrDefault(false)
            photoGate.withLock {
                photoInflight -= url
                if (!loaded) photoFailed += url
            }
        }
    }
}

private suspend fun loadStructurePhoto(url: String): Boolean {
    val key = structurePhotoKey(url)
    val cached = withContext(Dispatchers.Default) { runCatching { photoDisk.read(key) }.getOrNull() }
    val decoded = if (cached != null) {
        runCatching { decodeStructurePhoto(cached, PHOTO_MAX_EDGE) }.getOrNull()
    } else {
        null
    } ?: run {
        val bytes = downloadStructurePhoto(url) ?: return false
        val fresh = runCatching { decodeStructurePhoto(bytes, PHOTO_MAX_EDGE) }.getOrNull() ?: return false
        withContext(Dispatchers.Default) { runCatching { photoDisk.write(key, fresh.jpeg) } }
        fresh
    }
    publishStructurePhoto(url, decoded.bitmap)
    return true
}

private suspend fun publishStructurePhoto(url: String, bitmap: ImageBitmap) {
    val evicted = mutableListOf<String>()
    withContext(Dispatchers.Main.immediate) {
        structurePhotoBitmaps.remove(url)
        structurePhotoBitmaps[url] = bitmap
        while (structurePhotoBitmaps.size > PHOTO_CACHE_SIZE) {
            val eldest = structurePhotoBitmaps.keys.firstOrNull() ?: break
            if (eldest == url) break
            structurePhotoBitmaps.remove(eldest)
            evicted += eldest
        }
    }
    photoGate.withLock {
        photoMemory += url
        photoMemory -= evicted.toSet()
    }
}

private suspend fun downloadStructurePhoto(url: String): ByteArray? = runCatching {
    val response = photoClient.get(url)
    if (response.status.isSuccess()) response.bodyAsBytes() else null
}.getOrNull()

internal fun structurePhotoKey(url: String): String {
    val id = url.substringAfter("/files/", "").substringBefore("?").filter { it.isDigit() }
    return id.ifEmpty { url.hashCode().toUInt().toString(16) }
}

@Composable
fun StructureCover(
    structure: HealthStructure,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val url = structure.photoUrl?.takeIf { it.isNotBlank() }
    val bitmap = url?.let { structurePhotoBitmaps[it] }
    LaunchedEffect(url) {
        if (url != null) prefetchStructurePhotos(listOf(url))
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier.background(RepereLavender))
    }
}
