package dev.donhempsmyer.synapticpurge.data.depInject

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsDao
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsRepository
import dev.donhempsmyer.synapticpurge.data.collections.OfflineCollectionsRepository
import dev.donhempsmyer.synapticpurge.data.recordings.OfflineRecordingsRepository
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsDao
import dev.donhempsmyer.synapticpurge.data.RecordingsDatabase
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseHiltModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecordingsDatabase {
        return RecordingsDatabase.getDatabase(context)
    }

    @Provides
    fun provideRecordingsDao(database: RecordingsDatabase): RecordingsDao {
        return database.recordingsDao()
    }


    @Provides
    fun provideCollectionsDao(db: RecordingsDatabase): CollectionsDao =
        db.collectionsDao()

    @Provides
    @Singleton
    fun provideRecordingsRepository(recordingsDao: RecordingsDao): RecordingsRepository {
        return OfflineRecordingsRepository(recordingsDao)
    }

    @Provides
    @Singleton
    fun provideCollectionsRepository(collectionsDao: CollectionsDao): CollectionsRepository =
        OfflineCollectionsRepository(collectionsDao)
}