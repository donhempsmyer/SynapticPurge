package dev.donhempsmyer.synapticpurge.viewModels


import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.Recording
import dev.donhempsmyer.synapticpurge.data.RecordingsRepository
import dev.donhempsmyer.synapticpurge.helpers.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "PurgeViewModel"

@HiltViewModel
class PurgeViewModel @Inject constructor(
    private val repository: RecordingsRepository,
    private val app: Application
) : ViewModel() {

    private val audioRecorder by lazy { AudioRecorder(app) }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    // Stream recordings from Room
    val recordings: StateFlow<List<Recording>> =
        repository.getAllRecordingsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    // Tracks the DB row ID for the current recording session
    private val _activeRecordingId = MutableStateFlow<Long?>(null)

    init {
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { Log.d("Auth", "Signed in anonymously") }
            .addOnFailureListener { e -> Log.e("Auth", "Anonymous sign-in failed", e) }
    }

    private fun createAudioFile(): File {
        val dir = File(app.filesDir, "recordings").apply { mkdirs() }
        return File(dir, "purge_${System.currentTimeMillis()}.m4a")
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            stopAndTranscribe()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val outputFile = createAudioFile()
        val started = audioRecorder.start(outputFile)

        if (!started) {
            outputFile.delete()
            return
        }

        _isRecording.value = true

        // Insert a DB row and remember the ID for later updates
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = repository.insertRecording(
                    Recording(
                        filePath = outputFile.absolutePath,
                        fileName = outputFile.name,
                        transcription = "Recording..."
                    )
                )
                _activeRecordingId.value = id
            } catch (e: Exception) {
                Log.e(TAG, "DB insert failed; stopping recording", e)
                // If DB insert fails, stop recording + clean up file
                audioRecorder.stop()
                outputFile.delete()
                _isRecording.value = false
                _activeRecordingId.value = null
            }
        }
    }

    private fun stopAndTranscribe() {
        val file = audioRecorder.stop()
        _isRecording.value = false

        val id = _activeRecordingId.value
        _activeRecordingId.value = null

        if (file == null || id == null) return

        processAudioFile(recordingId = id, file = file)
    }

    private fun processAudioFile(recordingId: Long, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTranscription(recordingId, "Transcribing...")

                val audioBytes = file.readBytes()

                val prompt = content {
                    inlineData(audioBytes, "audio/m4a")
                    text("Please accurately transcribe this audio.")
                }

                val response = model.generateContent(prompt)
                val resultText = response.text ?: "No text returned."

                repository.updateTranscription(recordingId, resultText)

                _transcription.value = resultText

            } catch (e: Exception) {
                Log.e(TAG, "Transcription error", e)
                repository.updateTranscription(recordingId, "Error during transcription.")
                _transcription.value = "Error during transcription."
            } finally {
                _activeRecordingId.value = null
            }
        }
    }
}