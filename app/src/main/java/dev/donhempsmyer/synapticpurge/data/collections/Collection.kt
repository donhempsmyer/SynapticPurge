package dev.donhempsmyer.synapticpurge.data.collections


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val mode: String,
    val prompt: String? = null
)