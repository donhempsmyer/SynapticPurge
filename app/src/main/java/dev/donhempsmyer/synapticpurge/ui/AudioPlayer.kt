package dev.donhempsmyer.synapticpurge.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import kotlin.math.roundToInt

@Composable
fun AudioPlayer(
    playback: AudioPlayback,
    filePath: String,
    durationMsHint: Int = 0,
    modifier: Modifier = Modifier
) {
    val state by playback.state.collectAsState()

    LaunchedEffect(filePath) {
        playback.preloadDuration(filePath)
    }

    val isThisRow = state.filePath == filePath
    val isBuffering = isThisRow && state.isBuffering
    val isPlaying = isThisRow && state.isPlaying
    val isPrepared = isThisRow && state.isPrepared

    val duration = when {

        isThisRow && state.durationMs > 0 -> state.durationMs
        durationMsHint > 0 -> durationMsHint
        else -> 0
    }

    val position = if (isThisRow) state.positionMs else 0

    // Local slider state so dragging doesn't fight ticker updates
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPos by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isThisRow, position, duration, isScrubbing) {
        if (!isScrubbing) scrubPos = position.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(0f))
    }

    // Small “playing” animation (pulsing dot)
    val infinite = rememberInfiniteTransition(label = "playingPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = {
                        when {
                            isPlaying -> playback.pause()
                            isPrepared && isThisRow -> playback.resume()
                            else -> playback.play(filePath)
                        }
                    },
                    enabled = !isBuffering
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(
                    onClick = { playback.stop() },
                    enabled = isThisRow && (isPrepared || isPlaying || isBuffering)
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop")
                }

                Spacer(Modifier.width(8.dp))

                // Animated indicator
                if (isPlaying) {
                    Surface(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer(alpha = pulse),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                } else if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Spacer(Modifier.size(10.dp))
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = formatTime(position) + " / " + formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            Slider(
                value = scrubPos,
                onValueChange = {
                    isScrubbing = true
                    scrubPos = it
                },
                onValueChangeFinished = {
                    isScrubbing = false
                    playback.seekTo(scrubPos.roundToInt())
                },
                valueRange = 0f..(duration.toFloat().coerceAtLeast(0f)),
                enabled = isThisRow && isPrepared && duration > 0 && !isBuffering
            )

            if (isThisRow && state.error != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}