package dev.donhempsmyer.synapticpurge.helpers

import android.media.MediaMetadataRetriever

fun readDurationMs(filePath: String): Int {
    return try {
        val r = MediaMetadataRetriever()
        try {
            r.setDataSource(filePath)
            val d = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            d?.toIntOrNull() ?: 0
        } finally {
            r.release()
        }
    } catch (_: Exception) {
        0
    }
}