package dev.donhempsmyer.synapticpurge.ui.screens.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ConvertSource : Parcelable { TODAY, SELECTED }

@Parcelize
enum class ConvertMode : Parcelable { ARCHIVE, CONDENSE, CUSTOM }

@Parcelize
sealed class ConvertSheetStep : Parcelable {
    @Parcelize
    data object Hidden : ConvertSheetStep()

    @Parcelize
    data class Options(
        val source: ConvertSource,
        val selectedIdsSnapshot: List<Long> = emptyList()
    ) : ConvertSheetStep()

    @Parcelize
    data class Processing(
        val source: ConvertSource,
        val selectedIdsSnapshot: List<Long>,
        val mode: ConvertMode,
        val customPrompt: String,
        val attempt: Int = 0
    ) : ConvertSheetStep()

    @Parcelize
    data class Success(
        val createdCollectionId: Long = 0L,
        val processedRecordingIds: List<Long>,
        val resultText: String
    ) : ConvertSheetStep()
}