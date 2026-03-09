package dev.donhempsmyer.synapticpurge.data.collections


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(collection: Collection): Long

    @Update
    suspend fun update(collection: Collection)

    @Delete
    suspend fun delete(collection: Collection)

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollection(id: Long): Flow<Collection?>

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("""
        SELECT * FROM collections
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(content) LIKE '%' || LOWER(:query) || '%'
        ORDER BY createdAt DESC
        LIMIT 500
    """)
    fun searchCollections(query: String): Flow<List<Collection>>


    @Query("DELETE FROM collections WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int
}