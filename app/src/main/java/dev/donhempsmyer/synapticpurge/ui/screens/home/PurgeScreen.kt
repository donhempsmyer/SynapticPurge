package dev.donhempsmyer.synapticpurge.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.data.recordings.Recording
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.components.CenteredSectionHeader
import dev.donhempsmyer.synapticpurge.ui.components.formatSectionHeader
import dev.donhempsmyer.synapticpurge.ui.AudioPlayer
import dev.donhempsmyer.synapticpurge.ui.components.ExpandableSelectableCard
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun PurgeScreen(
    isRecording: Boolean,
    recordings: List<Recording>,
    isSelecting: Boolean,
    selectedIds: Set<Long>,
    onCheckboxClick: (Long) -> Unit,
    audioPlayback: AudioPlayback,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to newest item (top of list)
    LaunchedEffect(recordings.firstOrNull()?.id) {
        if (recordings.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(isSelecting) {
        if (isSelecting) audioPlayback.stop()
    }

    // Group recordings by date for sticky headers
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }

    val grouped: Map<LocalDate, List<Recording>> = remember(recordings) {
        recordings.groupBy { rec ->
            Instant.ofEpochMilli(rec.timestamp).atZone(zone).toLocalDate()
        }
    }

    val sectionDates: List<LocalDate> = remember(grouped, today) {
        val dates = grouped.keys.sortedDescending()
        if (dates.contains(today)) listOf(today) + dates.filter { it != today } else dates
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (recordings.isEmpty()) {
                    item {
                        Text(
                            "Your transcriptions will appear here...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    sectionDates.forEach { date ->

                        stickyHeader(key = "purge_header_$date") {
                            CenteredSectionHeader(text = formatSectionHeader(date, today))
                        }

                        val dayItems = grouped[date].orEmpty()

                        items(
                            items = dayItems,
                            key = { it.id }
                        ) { recording ->
                            RecordingRow(
                                recording = recording,
                                isSelecting = isSelecting,
                                isSelected = selectedIds.contains(recording.id),
                                onCheckboxClick = { onCheckboxClick(recording.id) },
                                audioPlayback = audioPlayback
                                )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                initialScale = 0.1f,
                transformOrigin = TransformOrigin(0.5f, 0.9f),
                animationSpec = tween(500)
            ),
            exit = fadeOut(animationSpec = tween(500)) + scaleOut(
                targetScale = 0.1f,
                transformOrigin = TransformOrigin(0.5f, 0.9f),
                animationSpec = tween(500)
            )
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "breathing_glow")
            val breathingAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing_alpha"
            )
            val breathingScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing_scale"
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier
                                .size(200.dp)
                                .graphicsLayer(
                                    scaleX = breathingScale * 1.5f,
                                    scaleY = breathingScale * 1.5f,
                                    alpha = breathingAlpha
                                ),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) { }

                        Text(
                            text = "Listening...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.graphicsLayer(
                                scaleX = breathingScale,
                                scaleY = breathingScale
                            )
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun RecordingRow(
    recording: Recording,
    isSelecting: Boolean,
    isSelected: Boolean,
    onCheckboxClick: () -> Unit,
    audioPlayback: AudioPlayback
) {
    val formattedTime = remember(recording.timestamp) {
        val local = Locale.getDefault()
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", local)
        sdf.format(Date(recording.timestamp))
    }

    ExpandableSelectableCard(
        id = recording.id,
        isSelecting = isSelecting,
        isSelected = isSelected,
        onCheckboxClick = onCheckboxClick,
        header = {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(recording.fileName, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                recording.transcription.ifBlank { "..." },
                maxLines = 3
            )
        },
        expandedBody = {

            Spacer(Modifier.height(8.dp))


            AudioPlayer(
                playback = audioPlayback,
                filePath = recording.filePath,
                durationMsHint = recording.durationMs,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    )
}

