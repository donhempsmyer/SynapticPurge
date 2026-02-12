package dev.donhempsmyer.synapticpurge.data.depInject

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.donhempsmyer.synapticpurge.data.OfflineRecordingsRepository
import dev.donhempsmyer.synapticpurge.data.RecordingsDao
import dev.donhempsmyer.synapticpurge.data.RecordingsDatabase
import dev.donhempsmyer.synapticpurge.data.RecordingsRepository
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
    @Singleton
    fun provideRecordingsRepository(recordingsDao: RecordingsDao): RecordingsRepository {
        return OfflineRecordingsRepository(recordingsDao)
    }
}