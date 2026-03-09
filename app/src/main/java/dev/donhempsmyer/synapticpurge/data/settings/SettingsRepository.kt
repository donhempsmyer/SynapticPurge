package dev.donhempsmyer.synapticpurge.data.settings


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DEFAULT_CONVERT_MODE = stringPreferencesKey("default_convert_mode")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val AUTO_STOP = stringPreferencesKey("auto_stop")

        val DEFAULT_DELETE_AUDIO = booleanPreferencesKey("default_delete_audio")
        val DEFAULT_DELETE_TRANSCRIPTION = booleanPreferencesKey("default_delete_transcription")

        val NO_WORDS_HANDLING = stringPreferencesKey("no_words_handling")

        val HAPTICS = booleanPreferencesKey("haptics")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<AppSettings> =
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                AppSettings(
                    defaultConvertMode = prefs[Keys.DEFAULT_CONVERT_MODE]
                        ?.let { runCatching { DefaultConvertMode.valueOf(it) }.getOrNull() }
                        ?: AppSettings().defaultConvertMode,

                    audioQuality = prefs[Keys.AUDIO_QUALITY]
                        ?.let { runCatching { AudioQualityPreset.valueOf(it) }.getOrNull() }
                        ?: AppSettings().audioQuality,

                    autoStop = prefs[Keys.AUTO_STOP]
                        ?.let { runCatching { AutoStopOption.valueOf(it) }.getOrNull() }
                        ?: AppSettings().autoStop,

                    defaultDeleteAudio = prefs[Keys.DEFAULT_DELETE_AUDIO] ?: AppSettings().defaultDeleteAudio,
                    defaultDeleteTranscription = prefs[Keys.DEFAULT_DELETE_TRANSCRIPTION] ?: AppSettings().defaultDeleteTranscription,

                    noWordsHandling = prefs[Keys.NO_WORDS_HANDLING]
                        ?.let { runCatching { NoWordsHandling.valueOf(it) }.getOrNull() }
                        ?: AppSettings().noWordsHandling,

                    hapticsEnabled = prefs[Keys.HAPTICS] ?: AppSettings().hapticsEnabled,

                    themeMode = prefs[Keys.THEME_MODE]
                        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                        ?: AppSettings().themeMode
                )
            }

    suspend fun setDefaultConvertMode(v: DefaultConvertMode) =
        context.dataStore.edit { it[Keys.DEFAULT_CONVERT_MODE] = v.name }

    suspend fun setAudioQuality(v: AudioQualityPreset) =
        context.dataStore.edit { it[Keys.AUDIO_QUALITY] = v.name }

    suspend fun setAutoStop(v: AutoStopOption) =
        context.dataStore.edit { it[Keys.AUTO_STOP] = v.name }

    suspend fun setDefaultDeleteAudio(v: Boolean) =
        context.dataStore.edit { it[Keys.DEFAULT_DELETE_AUDIO] = v }

    suspend fun setDefaultDeleteTranscription(v: Boolean) =
        context.dataStore.edit { it[Keys.DEFAULT_DELETE_TRANSCRIPTION] = v }

    suspend fun setNoWordsHandling(v: NoWordsHandling) =
        context.dataStore.edit { it[Keys.NO_WORDS_HANDLING] = v.name }

    suspend fun setHapticsEnabled(v: Boolean) =
        context.dataStore.edit { it[Keys.HAPTICS] = v }

    suspend fun setThemeMode(v: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME_MODE] = v.name }
}