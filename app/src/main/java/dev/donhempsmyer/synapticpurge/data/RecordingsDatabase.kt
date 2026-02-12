package dev.donhempsmyer.synapticpurge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recording::class], version = 1, exportSchema = false)
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
                    // .fallbackToDestructiveMigration() // only if prototyping
                    .build()
                    .also { INSTANCE = it }
            }
    }
}