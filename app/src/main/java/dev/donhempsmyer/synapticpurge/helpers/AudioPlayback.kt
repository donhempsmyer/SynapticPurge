package dev.donhempsmyer.synapticpurge.helpers

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AudioPlayback(
    private val scope: CoroutineScope
) {

    data class PlaybackState(
        val filePath: String? = null,
        val isPrepared: Boolean = false,
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val durationMs: Int = 0,
        val positionMs: Int = 0,
        val error: String? = null
    )

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    private var player: MediaPlayer? = null

    private val durationCache = ConcurrentHashMap<String, Int>() // ms
    private var tickerJob: Job? = null

    fun play(filePath: String) {
        // If same file and prepared, resume instead of re-creating
        val s = _state.value
        if (s.filePath == filePath && s.isPrepared && !s.isPlaying) {
            resume()
            return
        }

        stop()

        if (!File(filePath).exists()) {
            _state.value = PlaybackState(
                filePath = filePath,
                error = "Audio file missing"
            )
            return
        }

        _state.value = PlaybackState(filePath = filePath, isBuffering = true)

        val p = MediaPlayer()
        player = p

        try {
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )

            p.setDataSource(filePath)

            p.setOnPreparedListener { mp ->
                val duration = runCatching { mp.duration }.getOrDefault(0)
                _state.value = _state.value.copy(
                    isPrepared = true,
                    isBuffering = false,
                    durationMs = duration,
                    positionMs = 0,
                    error = null
                )
                mp.start()
                _state.value = _state.value.copy(isPlaying = true)
                startTicker()
            }

            p.setOnCompletionListener {
                // Snap to end
                val duration = _state.value.durationMs
                _state.value = _state.value.copy(isPlaying = false, positionMs = duration)
                stop() // releases resources + resets state
            }

            p.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayback", "MediaPlayer error what=$what extra=$extra")
                _state.value = _state.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    error = "Playback error (what=$what extra=$extra)"
                )
                stop()
                true
            }

            p.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioPlayback", "Failed to play $filePath", e)
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = e.localizedMessage ?: "Playback failed"
            )
            stop()
        }
    }

    fun pause() {
        val p = player ?: return
        runCatching {
            if (p.isPlaying) {
                p.pause()
                _state.value = _state.value.copy(isPlaying = false)
            }
        }
    }

    fun resume() {
        val p = player ?: return
        if (!_state.value.isPrepared) return
        runCatching {
            p.start()
            _state.value = _state.value.copy(isPlaying = true, isBuffering = false, error = null)
            startTicker()
        }
    }

    fun stop() {
        tickerJob?.cancel()
        tickerJob = null

        val p = player
        player = null

        if (p != null) {
            runCatching { p.stop() }
            runCatching { p.reset() }
            runCatching { p.release() }
        }

        _state.value = PlaybackState()
    }

    fun seekTo(positionMs: Int) {
        val p = player ?: return
        if (!_state.value.isPrepared) return

        val clamped = positionMs.coerceIn(0, _state.value.durationMs)
        runCatching {
            // Prefer the new signature when available
            p.seekTo(clamped)
            _state.value = _state.value.copy(positionMs = clamped)
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                val p = player ?: break
                val pos = runCatching { p.currentPosition }.getOrDefault(_state.value.positionMs)
                _state.value = _state.value.copy(positionMs = pos)
                delay(250)
            }
        }
    }

    fun preloadDuration(filePath: String) {
        if (durationCache.containsKey(filePath)) return

        val ms = runCatching {
            MediaMetadataRetriever().use { r ->
                r.setDataSource(filePath)
                val d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                d?.toIntOrNull() ?: 0
            }
        }.getOrDefault(0)

        durationCache[filePath] = ms
        // If UI is looking at this file and duration is unknown, update state
        val s = _state.value
        if (s.filePath == filePath && s.durationMs == 0 && ms > 0) {
            _state.value = s.copy(durationMs = ms)
        }
    }

    fun getCachedDurationMs(filePath: String): Int = durationCache[filePath] ?: 0
}