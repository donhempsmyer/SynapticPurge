package dev.donhempsmyer.synapticpurge.helpers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder (private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null


    fun start(file: File): Boolean {

        if (recorder != null) {
            Log.w("AudioRecorder", "start() called while already recording")
            return false
        }
        return try {
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                (MediaRecorder())
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
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
            recorder?.release()
            recorder = null
            currentFile = null
            false
        }
    }


    fun stop(): File? {

        val savedFile = currentFile

        if (recorder == null) {
            Log.w("AudioRecorder", "stop() called while not recording")
            return null
        }

        return try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            Log.d("AudioRecorder", "Recording saved: ${savedFile?.absolutePath}")
            savedFile
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to stop recording", e)
            savedFile?.delete()
            null
        } finally {
            recorder = null
            currentFile = null
        }
    }
}