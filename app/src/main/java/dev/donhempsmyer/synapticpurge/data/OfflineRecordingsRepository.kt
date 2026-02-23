package dev.donhempsmyer.synapticpurge.data

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow


class OfflineRecordingsRepository @Inject constructor(
    private val recordingsDao: RecordingsDao
) : RecordingsRepository {

    override fun getAllRecordingsStream(): Flow<List<Recording>> =
        recordingsDao.getAllRecordings()

    override fun getRecordingStream(id: Long): Flow<Recording?> =
        recordingsDao.getRecording(id)

    override fun getTodayVisibleRecordingsStream(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<Recording>> =
        recordingsDao.getTodayVisibleRecordings(startMillis, endMillis)

    override suspend fun getTodayVisibleRecordingsOnce(
        startMillis: Long,
        endMillis: Long
    ): List<Recording> =
        recordingsDao.getTodayVisibleRecordingsOnce(startMillis, endMillis)

    override suspend fun insertRecording(recording: Recording): Long =
        recordingsDao.insert(recording)

    override suspend fun updateRecording(recording: Recording) =
        recordingsDao.update(recording)

    override suspend fun updateTranscription(id: Long, transcription: String): Int =
        recordingsDao.updateTranscription(id, transcription)

    override suspend fun hideByIds(ids: List<Long>): Int =
        if (ids.isEmpty()) 0 else recordingsDao.hideByIds(ids)

    override suspend fun unhideByIds(ids: List<Long>): Int =
        if (ids.isEmpty()) 0 else recordingsDao.unhideByIds(ids)

    override suspend fun clearTranscriptionByIds(ids: List<Long>): Int =
        if (ids.isEmpty()) 0 else recordingsDao.clearTranscriptionByIds(ids)

    override suspend fun deleteByIds(ids: List<Long>): Int =
        if (ids.isEmpty()) 0 else recordingsDao.deleteByIds(ids)

    override suspend fun getRecordingOnce(id: Long): Recording? =
        recordingsDao.getRecordingOnce(id)

    override fun searchRecordingsStream(query: String): Flow<List<Recording>> =
        recordingsDao.searchRecordings(query)

}