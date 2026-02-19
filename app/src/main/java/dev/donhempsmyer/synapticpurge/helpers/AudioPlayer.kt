package dev.donhempsmyer.synapticpurge.helpers

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class AudioPlayer(private var context: Context) {

    private var player: MediaPlayer? = null

    fun play(filePath: String) {
        stop()

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

            p.setOnPreparedListener { it.start() }

            p.setOnCompletionListener {
                stop()
            }

            p.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "MediaPlayer error what=$what extra=$extra")
                stop()
                true
            }

            // Optional: force full volume (usually not necessary; volume is controlled by stream)
            // p.setVolume(1f, 1f)

            p.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to play $filePath", e)
            stop() // ensures release
        }
    }

    fun stop() {
        val p = player ?: return
        // stop() throws if not started; these are fine to ignore
        runCatching { p.stop() }
        runCatching { p.reset() }
        runCatching { p.release() }
        player = null
    }
}