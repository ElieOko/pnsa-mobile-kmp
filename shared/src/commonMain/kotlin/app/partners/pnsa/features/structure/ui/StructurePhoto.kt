package app.partners.pnsa.features.structure.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.partners.pnsa.core.ui.components.quietClick
import app.partners.pnsa.core.network.platformHttpClient
import app.partners.pnsa.core.ui.theme.RepereLavender
import app.partners.pnsa.core.ui.theme.ReperePurple
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
private class PhotoJob(val url: String, val ahead: Boolean)

private val photoQueue = ArrayDeque<PhotoJob>()
private val photoInflight = mutableSetOf<String>()
private val photoPromote = mutableSetOf<String>()
private val photoFailed = mutableSetOf<String>()
private val photoMemory = mutableSetOf<String>()
private val photoRetained = LinkedHashSet<String>()
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
                if (url in photoMemory || url in photoFailed) continue
                if (url in photoInflight) {
                    if (ahead) photoPromote += url
                    continue
                }
                photoQueue.removeAll { it.url == url }
                val job = PhotoJob(url, ahead)
                if (ahead) photoQueue.addFirst(job) else photoQueue.addLast(job)
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
            val job = photoGate.withLock {
                val next = photoQueue.removeFirstOrNull()
                if (next == null) {
                    photoWorkers--
                    null
                } else {
                    photoInflight += next.url
                    next
                }
            } ?: return@launch
            val loaded = runCatching { loadStructurePhoto(job.url, job.ahead) }.getOrDefault(false)
            photoGate.withLock {
                photoInflight -= job.url
                photoPromote -= job.url
                if (!loaded) photoFailed += job.url
            }
        }
    }
}

private suspend fun loadStructurePhoto(url: String, ahead: Boolean): Boolean {
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
    val show = ahead || photoGate.withLock { url in photoPromote }
    if (show) publishStructurePhoto(url, decoded.bitmap)
    return true
}

private suspend fun publishStructurePhoto(url: String, bitmap: ImageBitmap) {
    val evicted = mutableListOf<String>()
    withContext(Dispatchers.Main.immediate) {
        structurePhotoBitmaps.remove(url)
        structurePhotoBitmaps[url] = bitmap
        while (structurePhotoBitmaps.size > PHOTO_CACHE_SIZE) {
            val eldest = structurePhotoBitmaps.keys.firstOrNull { it != url && it !in photoRetained } ?: break
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

private fun retainStructurePhoto(url: String) {
    photoRetained.remove(url)
    photoRetained.add(url)
    while (photoRetained.size > 96) {
        photoRetained.remove(photoRetained.first())
    }
}

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
    var shown by remember(url) { mutableStateOf(bitmap) }
    if (bitmap != null) shown = bitmap
    var expanded by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url) {
        if (url != null) retainStructurePhoto(url)
    }
    LaunchedEffect(url, shown) {
        if (url != null && shown == null) prefetchStructurePhotos(listOf(url))
    }
    val open = modifier.quietClick { expanded = true }
    val display = shown
    if (display != null) {
        Image(
            bitmap = display,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = open,
        )
    } else {
        PhotoLoading(open)
    }
    if (expanded) {
        StructurePhotoViewer(display, contentDescription, onClose = { expanded = false })
    }
}

@Composable
private fun PhotoLoading(modifier: Modifier) {
    val transition = rememberInfiniteTransition()
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Box(
        modifier
            .background(RepereLavender)
            .drawBehind {
                val band = size.width * 0.7f
                val start = -band + (size.width + band * 2f) * shift
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White,
                            Color.Transparent,
                        ),
                        start = Offset(start, 0f),
                        end = Offset(start + band, size.height),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = ReperePurple,
            strokeWidth = 3.dp,
            modifier = Modifier.fillMaxSize(0.28f).padding(4.dp),
        )
    }
}

@Composable
private fun StructurePhotoViewer(
    bitmap: ImageBitmap?,
    contentDescription: String,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF16122C))
                .quietClick(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                )
            } else {
                PhotoLoading(
                    Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(24.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }
        }
    }
}
