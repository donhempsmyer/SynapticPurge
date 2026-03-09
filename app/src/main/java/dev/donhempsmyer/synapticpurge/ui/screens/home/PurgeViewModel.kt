package dev.donhempsmyer.synapticpurge.ui.screens.home


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
import dev.donhempsmyer.synapticpurge.data.collections.Collection
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsRepository
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsRepository
import dev.donhempsmyer.synapticpurge.helpers.AudioRecorder
import dev.donhempsmyer.synapticpurge.helpers.readDurationMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "PurgeViewModel"

@HiltViewModel
class PurgeViewModel @Inject constructor(
    private val repository: RecordingsRepository,
    private val collections: CollectionsRepository,
    private val app: Application
) : ViewModel() {

    private val audioRecorder by lazy { AudioRecorder(app) }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    data class ConversionResult(
        val processedIds: List<Long>,
        val resultText: String
    )

    val recordings: StateFlow<List<Recording>> =
        repository.getVisibleRecordingsStream()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

        val local = Locale.getDefault()
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", local)
        val stamp = sdf.format(Date())

        val suffix = java.util.UUID.randomUUID().toString().substring(0, 4).uppercase(local)

        return File(dir, "purge_${stamp}_$suffix.m4a")
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

        viewModelScope.launch(Dispatchers.IO) {
            val rec = repository.getRecordingOnce(id) ?: return@launch
            val duration = readDurationMs(rec.filePath)
            repository.updateRecording(rec.copy(durationMs = duration))
        }

        processAudioFile(recordingId = id, file = file)
    }

    private fun processAudioFile(recordingId: Long, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTranscription(recordingId, "Transcribing...")

                val audioBytes = file.readBytes()

                val prompt = content {
                    inlineData(audioBytes, "audio/m4a")
                    text("Please accurately transcribe this audio. If no words are detected, respond with 'No words detected.'")
                }

                val response = model.generateContent(prompt)
                val resultText = response.text ?: "No text returned."
                //val resultText = "Testing Animation"

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

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteByIds(listOf(recording.id))
            runCatching { File(recording.filePath).delete()}
        }
    }


    suspend fun runConversion(
        source: ConvertSource,
        selectedIds: List<Long>,
        mode: ConvertMode,
        customPrompt: String
    ): ConversionResult = withContext(Dispatchers.IO) {

        // 1) Get snapshot list (Once)
        val recordings = when (source) {
            ConvertSource.TODAY -> {
                val (startMillis, endMillis) = todayRangeMillis()
                repository.getTodayVisibleRecordingsOnce(startMillis, endMillis)
            }
            ConvertSource.SELECTED -> repository.getRecordingsByIdsOnce(selectedIds)
        }

        // 2) Filter out invalid transcriptions for processing
        val valid = recordings.filter { isValidTranscription(it.transcription) }

        val processedIds = valid.map { it.id }

        // If nothing to process, return a friendly message
        if (valid.isEmpty()) {
            return@withContext ConversionResult(
                processedIds = emptyList(),
                resultText = "No valid transcriptions to process."
            )
        }

        // 3) Build plain “archive text” input
        val archiveText = buildString {
            valid.sortedBy { it.timestamp }.forEach { r ->
                appendLine("• ${r.fileName}")
                appendLine(r.transcription.trim())
                appendLine()
            }
        }.trim()

        // 4) Mode handling
        when (mode) {
            ConvertMode.ARCHIVE -> {
                ConversionResult(processedIds, archiveText)
            }

            ConvertMode.CONDENSE -> {
                val prompt = """
                You are helping a user organize, with high-fidelity, a (or set of) short dictaphone transcription notes.

                Depending on the perceived context, convert these notes into either (or all):
                1) A short summary with bullet points 
                2) Actionable tasks (checkbox-style list)
                3) Key ideas / reminders
                
                Output rules:
                - Return ONLY the final result.
                - Do NOT include reasoning, analysis, hidden markup, or meta commentary.
                - Do NOT include headings like "Reasoning", "Analysis", or "Thoughts".
                - Do NOT wrap output in JSON or code fences.
                - Output MUST be plain text only.
                - Do NOT use Markdown symbols (*, **, #, -, [ ]).
                - Use simple labels like:
                  SUMMARY:
                  TASKS:
                  IDEAS:
                - For lists, use hyphen "-" only (no asterisks).
                - For tasks, use "( )" instead of "[ ]".

                Notes:
                $archiveText
            """.trimIndent()

                val response = model.generateContent(
                    content { text(prompt) }
                )
                ConversionResult(processedIds, response.text ?: "No text returned.")
            }

            ConvertMode.CUSTOM -> {
                val prompt = """
                Follow the user's instruction intuitively.
                
                Output rules:
                - Return ONLY the final result.
                - Do NOT include reasoning, analysis, hidden markup, or meta commentary.
                - Do NOT include headings like "Reasoning", "Analysis", or "Thoughts".
                - Do NOT wrap output in JSON or code fences.
                - Output MUST be plain text only.
                - Do NOT use Markdown symbols (*, **, #, -, [ ]).
                - Use simple labels like:
                  SUMMARY:
                  TASKS:
                  IDEAS:
                - For lists, use hyphen "-" only (no asterisks).
                - For tasks, use "( )" instead of "[ ]".

                User instruction:
                ${customPrompt.trim()}

                Notes:
                $archiveText
            """.trimIndent()

                val response = model.generateContent(
                    content { text(prompt) }
                )
                ConversionResult(processedIds, response.text ?: "No text returned.")
            }
        }
    }

    // Helpers (inside PurgeViewModel)
    private fun isValidTranscription(t: String): Boolean {
        val s = t.trim()
        if (s.isBlank()) return false

        val normalized = s.lowercase().removeSuffix(".")
        return normalized != "no words detected"
    }

    private fun todayRangeMillis(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    suspend fun insertCollection(
        title: String,
        content: String,
        mode: ConvertMode,
        prompt: String?
    ): Long = withContext(Dispatchers.IO) {
        collections.insertCollection(
            Collection(
                title = title.trim(),
                content = content,
                mode = mode.name,
                prompt = prompt
            )
        )
    }

    suspend fun hideFromPurgeAfterConvert(
        source: ConvertSource,
        selectedIdsSnapshot: List<Long>
    ) = withContext(Dispatchers.IO) {
        when (source) {
            ConvertSource.TODAY -> {
                val (startMillis, endMillis) = todayRangeMillis()
                val todayVisible = repository.getTodayVisibleRecordingsOnce(startMillis, endMillis)
                repository.hideByIds(todayVisible.map { it.id })
            }
            ConvertSource.SELECTED -> {
                repository.hideByIds(selectedIdsSnapshot)
            }
        }
    }


    suspend fun applyPostConvertActions(
        ids: List<Long>,
        deleteAudio: Boolean,
        deleteTranscription: Boolean
    ) = withContext(Dispatchers.IO) {

        if (ids.isEmpty()) return@withContext

        when {
            // AND rule: both toggles => delete row (and delete audio)
            deleteAudio && deleteTranscription -> {
                // best-effort delete files first
                val recs = repository.getRecordingsByIdsOnce(ids)
                recs.forEach { runCatching { File(it.filePath).delete() } }

                repository.deleteByIds(ids)
            }

            // XOR: delete audio only => delete files, keep rows
            deleteAudio -> {
                val recs = repository.getRecordingsByIdsOnce(ids)
                recs.forEach { runCatching { File(it.filePath).delete() } }
                // keep row unchanged (filePath remains, file may be missing — treat as success)
            }

            // XOR: delete transcription only => clear transcription, keep rows + audio
            deleteTranscription -> {
                repository.clearTranscriptionByIds(ids)
            }

            // neither => do nothing
            else -> Unit
        }

        // Always hide from Purge after conversion so Home list stays clean
        repository.hideByIds(ids)
    }
}