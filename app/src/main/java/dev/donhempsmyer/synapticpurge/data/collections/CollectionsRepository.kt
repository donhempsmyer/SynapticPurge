package dev.donhempsmyer.synapticpurge.data.collections


import kotlinx.coroutines.flow.Flow

interface CollectionsRepository {
    fun getAllCollectionsStream(): Flow<List<Collection>>
    fun getCollectionStream(id: Long): Flow<Collection?>
    fun searchCollectionsStream(query: String): Flow<List<Collection>>
    suspend fun insertCollection(collection: Collection): Long

    suspend fun deleteByIds(ids: List<Long>): Int
}