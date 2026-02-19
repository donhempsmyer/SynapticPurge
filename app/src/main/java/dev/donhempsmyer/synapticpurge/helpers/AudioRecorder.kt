package dev.donhempsmyer.synapticpurge.helpers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    @Synchronized
    fun start(file: File): Boolean {
        if (recorder != null) {
            Log.w("AudioRecorder", "start() called while already recording")
            return false
        }

        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }

        return try {
            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                // Add these for better quality and compatibility
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recorder = newRecorder
            currentFile = file
            Log.d("AudioRecorder", "Recording started: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            // If start fails, newRecorder must be released.
            releaseRecorder(newRecorder) // Use the helper
            recorder = null
            currentFile = null
            false
        }
    }

    @Synchronized
    fun stop(): File? {
        val recorderToStop = recorder
        val savedFile = currentFile

        if (recorderToStop == null) {
            Log.w("AudioRecorder", "stop() called while not recording")
            return null
        }

        var stopSucceeded = false
        try {
            // This can throw a RuntimeException
            recorderToStop.stop()
            stopSucceeded = true
            Log.d("AudioRecorder", "Recording stopped successfully")
        } catch (e: RuntimeException) {
            Log.e("AudioRecorder", "stop() failed. Deleting file.", e)
            // File is likely corrupted if stop failed.
            try { savedFile?.delete() } catch (_: Exception) {}
        } finally {
            // Always release the recorder, no matter what happened.
            releaseRecorder(recorderToStop)
            recorder = null
            currentFile = null
        }

        return if (stopSucceeded) savedFile else null
    }

    /**
     * Safely resets and releases the MediaRecorder instance.
     * This can be called from any state.
     */
    private fun releaseRecorder(recorderToRelease: MediaRecorder?) {
        recorderToRelease?.let {
            try {
                // Reset brings it out of an error state back to Idle.
                it.reset()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Failed to reset recorder", e)
            }
            try {
                // Release frees native resources.
                it.release()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Failed to release recorder", e)
            }
        }
    }
}
