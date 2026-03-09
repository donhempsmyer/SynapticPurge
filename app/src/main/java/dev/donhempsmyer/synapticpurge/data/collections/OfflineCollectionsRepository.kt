package dev.donhempsmyer.synapticpurge.data.collections


import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineCollectionsRepository @Inject constructor(
    private val collectionsDao: CollectionsDao
) : CollectionsRepository {

    override fun getAllCollectionsStream(): Flow<List<Collection>> =
        collectionsDao.getAllCollections()

    override fun getCollectionStream(id: Long): Flow<Collection?> =
        collectionsDao.getCollection(id)

    override fun searchCollectionsStream(query: String): Flow<List<Collection>> =
        collectionsDao.searchCollections(query)

    override suspend fun insertCollection(collection: Collection): Long =
        collectionsDao.insert(collection)

    override suspend fun deleteByIds(ids: List<Long>): Int =
        if (ids.isEmpty()) 0 else collectionsDao.deleteByIds(ids)
}