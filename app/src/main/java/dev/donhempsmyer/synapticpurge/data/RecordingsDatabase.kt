package dev.donhempsmyer.synapticpurge.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Recording::class],
    version = 2, exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class RecordingsDatabase : RoomDatabase() {

    abstract fun recordingsDao(): RecordingsDao

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