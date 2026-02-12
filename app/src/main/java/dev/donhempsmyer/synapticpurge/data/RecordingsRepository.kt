package dev.donhempsmyer.synapticpurge.data

import kotlinx.coroutines.flow.Flow


interface RecordingsRepository {
    fun getAllRecordingsStream(): Flow<List<Recording>>
    fun getRecordingStream(id: Long): Flow<Recording?>

    suspend fun insertRecording(recording: Recording): Long
    suspend fun deleteRecording(recording: Recording)
    suspend fun updateRecording(recording: Recording)

    suspend fun updateTranscription(id: Long, transcription: String)
    suspend fun getRecordingOnce(id: Long): Recording?
}