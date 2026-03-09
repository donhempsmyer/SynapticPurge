package dev.donhempsmyer.synapticpurge.ui.screens.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.donhempsmyer.synapticpurge.data.settings.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repo.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDefaultConvertMode(v: DefaultConvertMode) = viewModelScope.launch { repo.setDefaultConvertMode(v) }
    fun setAudioQuality(v: AudioQualityPreset) = viewModelScope.launch { repo.setAudioQuality(v) }
    fun setAutoStop(v: AutoStopOption) = viewModelScope.launch { repo.setAutoStop(v) }

    fun setDefaultDeleteAudio(v: Boolean) = viewModelScope.launch { repo.setDefaultDeleteAudio(v) }
    fun setDefaultDeleteTranscription(v: Boolean) = viewModelScope.launch { repo.setDefaultDeleteTranscription(v) }

    fun setNoWordsHandling(v: NoWordsHandling) = viewModelScope.launch { repo.setNoWordsHandling(v) }

    fun setHapticsEnabled(v: Boolean) = viewModelScope.launch { repo.setHapticsEnabled(v) }

    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { repo.setThemeMode(v) }
}