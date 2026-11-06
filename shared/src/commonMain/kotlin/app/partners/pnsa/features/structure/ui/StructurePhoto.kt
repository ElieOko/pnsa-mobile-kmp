package app.partners.pnsa.features.structure.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val PHOTO_MAX_EDGE = 960
private const val PHOTO_CACHE_SIZE = 36

private val photoClient = platformHttpClient {
    expectSuccess = false
    followRedirects = true
    install(HttpTimeout) {
        requestTimeoutMillis = 25_000
        connectTimeoutMillis = 12_000
    }
}

private val photoMutex = Mutex()
private val photoCache = LinkedHashMap<String, ImageBitmap>()

private suspend fun cachedPhoto(url: String): ImageBitmap? = photoMutex.withLock { photoCache[url] }

private suspend fun storePhoto(url: String, image: ImageBitmap) {
    photoMutex.withLock {
        photoCache.remove(url)
        photoCache[url] = image
        while (photoCache.size > PHOTO_CACHE_SIZE) {
            val eldest = photoCache.entries.firstOrNull()?.key ?: break
            photoCache.remove(eldest)
        }
    }
}

internal suspend fun loadStructurePhoto(url: String): ImageBitmap? {
    cachedPhoto(url)?.let { return it }
    val bytes = runCatching {
        val response = photoClient.get(url)
        if (!response.status.isSuccess()) return null
        response.bodyAsBytes()
    }.getOrNull() ?: return null
    val image = runCatching {
        withContext(Dispatchers.Default) { decodeStructurePhoto(bytes, PHOTO_MAX_EDGE) }
    }.getOrNull() ?: return null
    storePhoto(url, image)
    return image
}

expect fun decodeStructurePhoto(bytes: ByteArray, maxEdge: Int): ImageBitmap

@Composable
fun StructureCover(
    structure: HealthStructure,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val url = structure.photoUrl?.takeIf { it.isNotBlank() }
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = if (url == null) null else loadStructurePhoto(url)
    }
    val loaded = bitmap
    if (loaded != null) {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier.background(RepereLavender))
    }
}
