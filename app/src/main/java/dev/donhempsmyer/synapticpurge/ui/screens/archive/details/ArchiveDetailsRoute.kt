package dev.donhempsmyer.synapticpurge.ui.screens.archive.details

// ArchiveDetailsRoute.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.AudioPlayer
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.TopAction
import dev.donhempsmyer.synapticpurge.ui.components.TopBarSpec

@Composable
fun ArchiveDetailsRoute(
    recordingId: Long,
    onNavigateUp: () -> Unit,
    onChrome: (ScreenChrome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveDetailsViewModel = hiltViewModel()
) {
    val recording by viewModel.recording(recordingId).collectAsStateWithLifecycle(initialValue = null)

    val scope = rememberCoroutineScope()
    val playback = remember { AudioPlayback(scope) }
    DisposableEffect(Unit) { onDispose { playback.stop() } }

    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    // Publish the delete action into the AppRoot top bar
    SideEffect {
        onChrome(
            ScreenChrome(
                topBar = TopBarSpec(
                    actions = listOf(
                        TopAction(
                            contentDescription = "Delete recording",
                            icon = Icons.Filled.Delete,
                            onClick = { showDeleteConfirm = true }
                        )
                    )
                ),
                bottomBar = BottomBarSpec.None
            )
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete recording?") },
            text = { Text("This will remove the recording from the database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteRecordingById(recordingId)
                        onNavigateUp()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }


    if (recording == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val r: Recording = recording!!

            item {
                Text(
                    text = r.fileName,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = formatTimestamp(r.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = r.transcription.ifBlank { "(Transcription deleted)" },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                AudioPlayer(
                    playback = playback,
                    filePath = r.filePath,
                    durationMsHint = r.durationMs,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // item {
            //     Text(
            //         text = r.filePath,
            //         style = MaterialTheme.typography.bodySmall,
            //         color = MaterialTheme.colorScheme.onSurfaceVariant
            //     )
            // }

        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}