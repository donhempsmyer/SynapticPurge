package dev.donhempsmyer.synapticpurge.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.donhempsmyer.synapticpurge.data.collections.Collection
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsDao
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsDao

@Database(
    entities = [Recording::class, Collection::class],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
    ]
)
abstract class RecordingsDatabase : RoomDatabase() {

    abstract fun recordingsDao(): RecordingsDao
    abstract fun collectionsDao(): CollectionsDao

    companion object {
        @Volatile private var INSTANCE: RecordingsDatabase? = null

        fun getDatabase(context: Context): RecordingsDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RecordingsDatabase::class.java,
                    "recordings_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}