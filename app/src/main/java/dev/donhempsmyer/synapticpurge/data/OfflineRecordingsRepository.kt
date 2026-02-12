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

    override suspend fun insertRecording(recording: Recording): Long =
        recordingsDao.insert(recording)

    override suspend fun deleteRecording(recording: Recording) =
        recordingsDao.delete(recording)

    override suspend fun updateRecording(recording: Recording) =
        recordingsDao.update(recording)

    override suspend fun updateTranscription(id: Long, transcription: String) =
        recordingsDao.updateTranscription(id, transcription)

    override suspend fun getRecordingOnce(id: Long): Recording? =
        recordingsDao.getRecordingOnce(id)
}
