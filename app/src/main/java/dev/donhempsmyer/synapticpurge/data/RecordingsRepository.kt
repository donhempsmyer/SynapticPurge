package dev.donhempsmyer.synapticpurge.data

import kotlinx.coroutines.flow.Flow


interface RecordingsRepository {
    fun getAllRecordingsStream(): Flow<List<Recording>>
    fun getRecordingStream(id: Long): Flow<Recording?>

    fun getTodayVisibleRecordingsStream(startMillis: Long, endMillis: Long): Flow<List<Recording>>
    suspend fun getTodayVisibleRecordingsOnce(startMillis: Long, endMillis: Long): List<Recording>

    suspend fun insertRecording(recording: Recording): Long
    suspend fun updateRecording(recording: Recording)
    suspend fun updateTranscription(id: Long, transcription: String): Int

    suspend fun hideByIds(ids: List<Long>): Int
    suspend fun unhideByIds(ids: List<Long>): Int
    suspend fun clearTranscriptionByIds(ids: List<Long>): Int
    suspend fun deleteByIds(ids: List<Long>): Int

    suspend fun getRecordingOnce(id: Long): Recording?

    fun searchRecordingsStream(query: String): Flow<List<Recording>>
}