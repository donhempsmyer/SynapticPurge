package dev.donhempsmyer.synapticpurge.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recordings",
    indices = [Index(value = ["filePath"], unique = true)]
)
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String,
    val fileName: String,
    val transcription: String = ""
)