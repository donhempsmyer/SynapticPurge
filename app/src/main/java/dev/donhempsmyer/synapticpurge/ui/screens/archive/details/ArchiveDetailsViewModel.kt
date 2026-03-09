package dev.donhempsmyer.synapticpurge.ui.screens.archive.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ArchiveDetailsViewModel @Inject constructor(
    private val repository: RecordingsRepository
) : ViewModel() {

    fun recording(id: Long): Flow<Recording?> = repository.getRecordingStream(id)

    fun deleteRecordingById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val rec = repository.getRecordingOnce(id) ?: return@launch
            repository.deleteByIds(listOf(id))
            runCatching { File(rec.filePath).delete() }
        }
    }
}