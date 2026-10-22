package app.partners.pnsa.core.audio

import androidx.compose.runtime.Composable

data class VoicePayload(
    val durationSeconds: Int,
    val audioPath: String? = null,
)

expect class PlatformVoiceRecorder() {
    fun startRecording(): String?
    fun stopRecording(): String?
    fun cancelRecording()
    fun maxAmplitude(): Int
}

expect class PlatformVoicePlayer() {
    fun play(filePath: String?, onCompletion: () -> Unit): Boolean
    fun pause()
    fun stop()
    fun seekTo(positionMs: Int)
    fun currentPositionMs(): Int
    fun durationMs(): Int
    fun isPlaying(): Boolean
}

@Composable
expect fun rememberEnsureAudioPermission(onResult: (Boolean) -> Unit): () -> Unit

fun formatVoiceTimer(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

object VoiceMessageCodec {
    private const val PREFIX = "[VOICE_NOTE|"

    fun encode(durationSeconds: Int, audioPath: String?): String {
        val safeDuration = durationSeconds.coerceAtLeast(1)
        val safePath = audioPath?.replace("|", "")?.replace("]", "").orEmpty()
        return "$PREFIX$safeDuration|$safePath]"
    }

    fun decode(text: String?, explicitDuration: Int? = null, explicitPath: String? = null): VoicePayload? {
        if (explicitDuration != null && explicitDuration > 0) {
            return VoicePayload(
                durationSeconds = explicitDuration,
                audioPath = explicitPath?.takeIf { it.isNotBlank() },
            )
        }
        val raw = text?.trim().orEmpty()
        if (!raw.startsWith(PREFIX) || !raw.endsWith("]")) return null
        val body = raw.removePrefix(PREFIX).removeSuffix("]")
        val parts = body.split("|", limit = 2)
        val duration = parts.firstOrNull()?.toIntOrNull() ?: return null
        val path = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
        return VoicePayload(
            durationSeconds = duration.coerceAtLeast(1),
            audioPath = explicitPath?.takeIf { it.isNotBlank() } ?: path,
        )
    }

    fun previewLabel(text: String?, explicitDuration: Int? = null, explicitPath: String? = null): String {
        val payload = decode(text, explicitDuration, explicitPath) ?: return text.orEmpty()
        return "Message vocal (${formatVoiceTimer(payload.durationSeconds)})"
    }
}
