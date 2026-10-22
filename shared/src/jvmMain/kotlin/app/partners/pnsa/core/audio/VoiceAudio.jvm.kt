package app.partners.pnsa.core.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit

actual class PlatformVoiceRecorder actual constructor() {
    actual fun startRecording(): String? = null
    actual fun stopRecording(): String? = null
    actual fun cancelRecording() {}
    actual fun maxAmplitude(): Int = 50
}

actual class PlatformVoicePlayer actual constructor() {
    actual fun play(filePath: String?, onCompletion: () -> Unit): Boolean {
        runCatching { Toolkit.getDefaultToolkit().beep() }
        return false
    }
    actual fun pause() {}
    actual fun stop() {}
    actual fun seekTo(positionMs: Int) {}
    actual fun currentPositionMs(): Int = 0
    actual fun durationMs(): Int = 0
    actual fun isPlaying(): Boolean = false
}

@Composable
actual fun rememberEnsureAudioPermission(onResult: (Boolean) -> Unit): () -> Unit =
    remember { { onResult(true) } }
