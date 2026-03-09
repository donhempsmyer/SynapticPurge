package dev.donhempsmyer.synapticpurge.data.settings

enum class DefaultConvertMode { ARCHIVE, CONDENSE, CUSTOM }
enum class AudioQualityPreset { STANDARD, HIGH }
enum class AutoStopOption(val seconds: Int) { OFF(0), S30(30), S60(60), M120(120) }
enum class NoWordsHandling { EXCLUDE_FROM_AI_AND_ARCHIVE, EXCLUDE_FROM_AI_ONLY, INCLUDE_ANYWAY }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val defaultConvertMode: DefaultConvertMode = DefaultConvertMode.CONDENSE,
    val audioQuality: AudioQualityPreset = AudioQualityPreset.STANDARD,
    val autoStop: AutoStopOption = AutoStopOption.OFF,

    // Default deletion policy (prefill sheet toggles)
    val defaultDeleteAudio: Boolean = false,
    val defaultDeleteTranscription: Boolean = false,

    val noWordsHandling: NoWordsHandling = NoWordsHandling.EXCLUDE_FROM_AI_AND_ARCHIVE,

    val hapticsEnabled: Boolean = true,

    val themeMode: ThemeMode = ThemeMode.SYSTEM
)