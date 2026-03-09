package dev.donhempsmyer.synapticpurge.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.recordings.RecordingsRepository
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: RecordingsRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordings: StateFlow<List<Recording>> =
        query
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { q ->
                if (q.isBlank()) repository.getAllRecordingsStream()
                else repository.searchRecordingsStream(q.trim())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onQueryChange(newValue: String) {
        _query.value = newValue
    }

    fun deleteRecordingsByIds(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteByIds(ids)
        }
    }
}