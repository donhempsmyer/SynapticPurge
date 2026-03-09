package dev.donhempsmyer.synapticpurge.ui.screens.archive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.components.CenteredSectionHeader
import dev.donhempsmyer.synapticpurge.ui.components.formatSectionHeader
import dev.donhempsmyer.synapticpurge.ui.AudioPlayer
import dev.donhempsmyer.synapticpurge.ui.components.ExpandableSelectableCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ArchiveScreen(
    recordings: List<Recording>,
    onOpenRecording: (Long) -> Unit,
    audioPlayback: AudioPlayback,
    isSelecting: Boolean,
    selectedIds: Set<Long>,
    onCheckboxClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }

    val grouped = remember(recordings) {
        recordings.groupBy { r ->
            Instant.ofEpochMilli(r.timestamp).atZone(zone).toLocalDate()
        }
    }

    val sectionDates = remember(grouped, today) {
        val dates = grouped.keys.sortedDescending()
        if (dates.contains(today)) listOf(today) + dates.filter { it != today } else dates
    }

    LaunchedEffect(isSelecting) {
        if (isSelecting) audioPlayback.stop()
    }


    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        sectionDates.forEach { date ->
            stickyHeader(key = "archive_header_$date") {
                CenteredSectionHeader(text = formatSectionHeader(date, today))
            }

            val dayItems = grouped[date].orEmpty()

            items(dayItems, key = { it.id }) { rec ->
                ArchiveRecordingRow(
                    recording = rec,
                    isSelecting = isSelecting,
                    isSelected = selectedIds.contains(rec.id),
                    onCheckboxClick = { onCheckboxClick(rec.id) },
                    onOpenDetails = { onOpenRecording(rec.id) },
                    audioPlayback = audioPlayback
                )
            }
        }
    }
}

@Composable
private fun ArchiveRecordingRow(
    recording: Recording,
    isSelecting: Boolean,
    isSelected: Boolean,
    onCheckboxClick: () -> Unit,
    onOpenDetails: () -> Unit,
    audioPlayback: AudioPlayback
) {
    ExpandableSelectableCard(
        id = recording.id,
        isSelecting = isSelecting,
        isSelected = isSelected,
        onCheckboxClick = onCheckboxClick,
        header = {
            Text(text = recording.fileName, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = recording.transcription.ifBlank { "(Transcription deleted)" },
                maxLines = 2
            )
        },
        expandedBody = {
            // Details button (appears only when expanded AND not selecting)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onOpenDetails) { Text("Details") }
            }

            // AudioPlayer (hidden automatically during selection by ExpandableSelectableCard)
            AudioPlayer(
                playback = audioPlayback,
                filePath = recording.filePath,
                durationMsHint = recording.durationMs,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    )
}

