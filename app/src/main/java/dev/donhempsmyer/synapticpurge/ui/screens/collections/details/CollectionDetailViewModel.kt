package dev.donhempsmyer.synapticpurge.ui.screens.collections.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.collections.Collection
import dev.donhempsmyer.synapticpurge.data.collections.CollectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val repository: CollectionsRepository
) : ViewModel() {
    fun collection(id: Long): Flow<Collection?> = repository.getCollectionStream(id)

    fun deleteCollectionById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteByIds(listOf(id))
        }
    }
}