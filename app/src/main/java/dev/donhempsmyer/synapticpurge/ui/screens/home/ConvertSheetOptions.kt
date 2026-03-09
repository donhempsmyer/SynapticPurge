package dev.donhempsmyer.synapticpurge.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConvertSheetOptions(
    source: ConvertSource,
    selectedCount: Int,
    mode: ConvertMode,
    onModeChange: (ConvertMode) -> Unit,
    customPrompt: String,
    onPromptChange: (String) -> Unit,
    onCancel: () -> Unit,
    onProcess: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Create Collection", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(8.dp))
        val usingText = when (source) {
            ConvertSource.TODAY -> "Using: Today"
            ConvertSource.SELECTED -> "Using: $selectedCount selected"
        }
        Text(usingText, style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))
        Text("Mode", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))

        ModeRadio("Archive (no AI)", mode == ConvertMode.ARCHIVE) { onModeChange(ConvertMode.ARCHIVE) }
        ModeRadio("Condense (AI)", mode == ConvertMode.CONDENSE) { onModeChange(ConvertMode.CONDENSE) }
        ModeRadio("Custom prompt", mode == ConvertMode.CUSTOM) { onModeChange(ConvertMode.CUSTOM) }

        if (mode == ConvertMode.CUSTOM) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = customPrompt,
                onValueChange = onPromptChange,
                label = { Text("Custom prompt") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onProcess,
                enabled = !(mode == ConvertMode.CUSTOM && customPrompt.isBlank())
            ) { Text("Process") }
        }
    }
}

@Composable
private fun ModeRadio(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ConvertSheetProcessing() {
    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Processing…", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ConvertSheetSuccess(
    title: String,
    onTitleChange: (String) -> Unit,
    deleteAudio: Boolean,
    onDeleteAudioChange: (Boolean) -> Unit,
    deleteTranscription: Boolean,
    onDeleteTranscriptionChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Collection created", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Collection title (required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = deleteAudio, onCheckedChange = onDeleteAudioChange)
            Text("Delete original audio")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = deleteTranscription, onCheckedChange = onDeleteTranscriptionChange)
            Text("Delete original transcription")
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onSubmit, enabled = title.isNotBlank()) { Text("Submit") }
        }
    }
}