package dev.donhempsmyer.synapticpurge.ui.screens.collections


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.collections.Collection
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repo: CollectionsRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val collections: StateFlow<List<Collection>> =
        _query
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { q ->
                if (q.isBlank()) repo.getAllCollectionsStream()
                else repo.searchCollectionsStream(q.trim())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun deleteCollectionsByIds(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteByIds(ids)
        }
    }
}