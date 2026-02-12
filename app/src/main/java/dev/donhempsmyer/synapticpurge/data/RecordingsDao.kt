package dev.donhempsmyer.synapticpurge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


import androidx.room.*

@Dao
interface RecordingsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recording: Recording): Long  // rowId

    @Update
    suspend fun update(recording: Recording)

    @Delete
    suspend fun delete(recording: Recording)

    @Query("SELECT * FROM recordings WHERE id = :id")
    fun getRecording(id: Long): Flow<Recording?>

    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    @Query("UPDATE recordings SET transcription = :transcription WHERE id = :id")
    suspend fun updateTranscription(id: Long, transcription: String)

    @Query("SELECT * FROM recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingOnce(id: Long): Recording?
}