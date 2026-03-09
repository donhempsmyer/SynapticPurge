package dev.donhempsmyer.synapticpurge.ui.screens.settings


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.data.settings.*

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreen(
        settings = settings,
        onDefaultConvertMode = viewModel::setDefaultConvertMode,
        onAudioQuality = viewModel::setAudioQuality,
        onAutoStop = viewModel::setAutoStop,
        onDefaultDeleteAudio = viewModel::setDefaultDeleteAudio,
        onDefaultDeleteTranscription = viewModel::setDefaultDeleteTranscription,
        onNoWordsHandling = viewModel::setNoWordsHandling,
        onHaptics = viewModel::setHapticsEnabled,
        onThemeMode = viewModel::setThemeMode,
        modifier = modifier
    )
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onDefaultConvertMode: (DefaultConvertMode) -> Unit,
    onAudioQuality: (AudioQualityPreset) -> Unit,
    onAutoStop: (AutoStopOption) -> Unit,
    onDefaultDeleteAudio: (Boolean) -> Unit,
    onDefaultDeleteTranscription: (Boolean) -> Unit,
    onNoWordsHandling: (NoWordsHandling) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsSection(
                title = "Conversion defaults",
                subtitle = "Choose how conversions behave by default."
            ) {
                RadioGroupRow(
                    title = "Default convert mode",
                    selected = settings.defaultConvertMode,
                    options = DefaultConvertMode.entries,
                    labelFor = {
                        when (it) {
                            DefaultConvertMode.ARCHIVE -> "Archive (no AI)"
                            DefaultConvertMode.CONDENSE -> "Condense (AI)"
                            DefaultConvertMode.CUSTOM -> "Custom prompt"
                        }
                    },
                    onSelect = onDefaultConvertMode
                )
            }
        }

        item {
            SettingsSection(
                title = "Recording",
                subtitle = "Audio quality and automatic stop."
            ) {
                RadioGroupRow(
                    title = "Audio quality preset",
                    selected = settings.audioQuality,
                    options = AudioQualityPreset.entries,
                    labelFor = { if (it == AudioQualityPreset.STANDARD) "Standard" else "High" },
                    onSelect = onAudioQuality
                )

                SectionDivider()

                RadioGroupRow(
                    title = "Auto-stop recording",
                    selected = settings.autoStop,
                    options = AutoStopOption.entries,
                    labelFor = {
                        when (it) {
                            AutoStopOption.OFF -> "Off"
                            AutoStopOption.S30 -> "30 seconds"
                            AutoStopOption.S60 -> "60 seconds"
                            AutoStopOption.M120 -> "2 minutes"
                        }
                    },
                    onSelect = onAutoStop
                )
            }
        }

        item {
            SettingsSection(
                title = "Deletion defaults",
                subtitle = "Used to prefill the conversion sheet."
            ) {
                SwitchListRow(
                    title = "Delete audio after submit",
                    subtitle = "Removes the original audio file.",
                    checked = settings.defaultDeleteAudio,
                    onCheckedChange = onDefaultDeleteAudio
                )

                SectionDivider()

                SwitchListRow(
                    title = "Delete transcription after submit",
                    subtitle = "Clears text; keeps the row unless audio is also deleted.",
                    checked = settings.defaultDeleteTranscription,
                    onCheckedChange = onDefaultDeleteTranscription
                )
            }
        }

        item {
            SettingsSection(
                title = "\"No words detected\" handling",
                subtitle = "Controls how empty/silent recordings are treated."
            ) {
                RadioGroupRow(
                    title = "Handling",
                    selected = settings.noWordsHandling,
                    options = NoWordsHandling.entries,
                    labelFor = {
                        when (it) {
                            NoWordsHandling.EXCLUDE_FROM_AI_AND_ARCHIVE -> "Exclude from AI + archive"
                            NoWordsHandling.EXCLUDE_FROM_AI_ONLY -> "Exclude from AI only"
                            NoWordsHandling.INCLUDE_ANYWAY -> "Include anyway"
                        }
                    },
                    onSelect = onNoWordsHandling
                )
            }
        }

        item {
            SettingsSection(
                title = "UI",
                subtitle = "Feedback and theme."
            ) {
                SwitchListRow(
                    title = "Haptics",
                    subtitle = "Vibration feedback for key actions.",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onHaptics
                )

                SectionDivider()

                RadioGroupRow(
                    title = "Theme",
                    selected = settings.themeMode,
                    options = ThemeMode.entries,
                    labelFor = {
                        when (it) {
                            ThemeMode.SYSTEM -> "System"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        }
                    },
                    onSelect = onThemeMode
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SwitchListRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle)
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun <T : Enum<T>> RadioGroupRow(
    title: String,
    selected: T,
    options: List<T>,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))

    options.forEachIndexed { idx, opt ->
        ListItem(
            headlineContent = { Text(labelFor(opt)) },
            leadingContent = {
                RadioButton(
                    selected = (opt == selected),
                    onClick = { onSelect(opt) }
                )
            }
        )
        if (idx != options.lastIndex) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}