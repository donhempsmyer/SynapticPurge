package dev.donhempsmyer.synapticpurge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface RecordingsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recording: Recording): Long

    @Update
    suspend fun update(recording: Recording)

    @Delete
    suspend fun delete(recording: Recording)

    @Query("SELECT * FROM recordings WHERE id = :id")
    fun getRecording(id: Long): Flow<Recording?>

    @Query("SELECT * FROM recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingOnce(id: Long): Recording?

    // Archive base list (you will group by date in UI)
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    // Today visible list for Purge screen
    @Query("""
        SELECT * FROM recordings
        WHERE hidden_from_purge = 0
          AND timestamp >= :startMillis
          AND timestamp < :endMillis
        ORDER BY timestamp DESC
    """)
    fun getTodayVisibleRecordings(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<Recording>>


    @Query("""
        SELECT * FROM recordings
        WHERE hidden_from_purge = 0
          AND timestamp >= :startMillis
          AND timestamp < :endMillis
        ORDER BY timestamp DESC
    """)
    suspend fun getTodayVisibleRecordingsOnce(
        startMillis: Long,
        endMillis: Long
    ): List<Recording>

    // Hide/unhide used to "clear" Purge after conversion
    @Query("UPDATE recordings SET hidden_from_purge = 1 WHERE id IN (:ids)")
    suspend fun hideByIds(ids: List<Long>): Int

    @Query("UPDATE recordings SET hidden_from_purge = 0 WHERE id IN (:ids)")
    suspend fun unhideByIds(ids: List<Long>): Int

    // Clear transcription (XOR rule)
    @Query("UPDATE recordings SET transcription = '' WHERE id IN (:ids)")
    suspend fun clearTranscriptionByIds(ids: List<Long>): Int

    // Delete rows (AND rule)
    @Query("DELETE FROM recordings WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("UPDATE recordings SET transcription = :transcription WHERE id = :id")
    suspend fun updateTranscription(id: Long, transcription: String): Int



    @Query("""
    SELECT * FROM recordings
    WHERE
        LOWER(fileName) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(transcription) LIKE '%' || LOWER(:query) || '%'
    ORDER BY timestamp DESC
    LIMIT 500
""")
    fun searchRecordings(query: String): Flow<List<Recording>>
}