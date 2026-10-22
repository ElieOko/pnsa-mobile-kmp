package app.partners.pnsa.core.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.partners.pnsa.AndroidAppContext
import java.io.File
import kotlin.math.sin

actual class PlatformVoiceRecorder actual constructor() {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    actual fun startRecording(): String? {
        cancelRecording()
        if (!AndroidAppContext.isBound) return null
        val context = AndroidAppContext.application
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) return null

        return runCatching {
            val dir = File(context.filesDir, "voices").apply { mkdirs() }
            val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioSamplingRate(44100)
            mediaRecorder.setAudioEncodingBitRate(96000)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            outputFile = file
            file.absolutePath
        }.getOrElse {
            releaseRecorder()
            null
        }
    }

    actual fun stopRecording(): String? {
        val file = outputFile
        val active = recorder
        recorder = null
        outputFile = null
        if (active == null) return null
        return runCatching {
            active.stop()
            active.release()
            file?.takeIf { it.exists() && it.length() > 0L }?.absolutePath
        }.getOrElse {
            runCatching { active.release() }
            runCatching { file?.delete() }
            null
        }
    }

    actual fun cancelRecording() {
        val file = outputFile
        releaseRecorder()
        runCatching { file?.delete() }
        outputFile = null
    }

    actual fun maxAmplitude(): Int {
        val raw = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        return ((raw / 327.67).toInt()).coerceIn(0, 100)
    }

    private fun releaseRecorder() {
        val active = recorder ?: return
        recorder = null
        runCatching { active.stop() }
        runCatching { active.release() }
    }
}

actual class PlatformVoicePlayer actual constructor() {
    private var player: MediaPlayer? = null
    private var currentPath: String? = null
    private var paused: Boolean = false
    private var fallbackTrack: AudioTrack? = null

    actual fun play(filePath: String?, onCompletion: () -> Unit): Boolean {
        if (!filePath.isNullOrBlank()) {
            val file = File(filePath)
            if (file.exists() && file.length() > 0L) {
                val existing = player
                if (existing != null && currentPath == filePath && paused) {
                    return runCatching {
                        existing.start()
                        paused = false
                        true
                    }.getOrDefault(false)
                }
                stop()
                return runCatching {
                    val mp = MediaPlayer()
                    mp.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    )
                    mp.setDataSource(file.absolutePath)
                    mp.prepare()
                    mp.setOnCompletionListener {
                        paused = false
                        onCompletion()
                    }
                    mp.start()
                    player = mp
                    currentPath = filePath
                    paused = false
                    true
                }.getOrElse {
                    stop()
                    playSynthesizedTone()
                    false
                }
            }
        }
        playSynthesizedTone()
        return false
    }

    private fun playSynthesizedTone() {
        runCatching {
            stopSynthesizedTone()
            val sampleRate = 16000
            val numSamples = sampleRate / 2
            val pcm = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val freq = if (i < numSamples / 2) 440.0 else 554.37
                val env = 1.0 - (i.toDouble() / numSamples)
                pcm[i] = (sin(2.0 * Math.PI * i * freq / sampleRate) * 8000 * env).toInt().toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            fallbackTrack = track
        }
    }

    private fun stopSynthesizedTone() {
        val track = fallbackTrack ?: return
        fallbackTrack = null
        runCatching { track.stop() }
        runCatching { track.release() }
    }

    actual fun pause() {
        stopSynthesizedTone()
        val mp = player ?: return
        runCatching {
            if (mp.isPlaying) {
                mp.pause()
                paused = true
            }
        }
    }

    actual fun stop() {
        stopSynthesizedTone()
        val mp = player
        player = null
        currentPath = null
        paused = false
        if (mp != null) {
            runCatching { mp.stop() }
            runCatching { mp.release() }
        }
    }

    actual fun seekTo(positionMs: Int) {
        runCatching { player?.seekTo(positionMs.coerceAtLeast(0)) }
    }

    actual fun currentPositionMs(): Int =
        runCatching { player?.currentPosition ?: 0 }.getOrDefault(0)

    actual fun durationMs(): Int =
        runCatching { player?.duration ?: 0 }.getOrDefault(0)

    actual fun isPlaying(): Boolean =
        runCatching { player?.isPlaying == true }.getOrDefault(false)
}

@Composable
actual fun rememberEnsureAudioPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onResult(granted)
    }
    return remember(context, launcher) {
        {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                onResult(true)
            } else {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
