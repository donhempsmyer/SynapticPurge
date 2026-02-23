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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.data.Recording
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun PurgeScreen(
    isRecording: Boolean,
    recordings: List<Recording>,
    onPlayRecording: (Recording) -> Unit,
    onDeleteRecording: (Recording) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (recordings.isEmpty()) {
                    item {
                        Text(
                            "Your transcriptions will appear here...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(
                        items = recordings,
                        key = { it.id }
                    ) { recording ->
                        RecordingRow(
                            recording = recording,
                            onPlayClick = onPlayRecording,
                            onDeleteClick = onDeleteRecording
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                initialScale = 0.1f, // Start very small (button sized)
                transformOrigin = TransformOrigin(0.5f, 0.9f),
                animationSpec = tween(500)
            ),
            exit = fadeOut(animationSpec = tween(500)) + scaleOut(
                targetScale = 0.1f,
                transformOrigin = TransformOrigin(0.5f, 0.9f),
                animationSpec = tween(500)
            )
        ) {
            //setup animation loop for the circle
            val infiniteTransition = rememberInfiniteTransition(label = "breathing_glow")
            val breathingAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse // from 1 back to 0.5
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
    onPlayClick: (Recording) -> Unit,
    onDeleteClick: (Recording) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedTime = remember(recording.timestamp) {
        val local = Locale.getDefault()
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", local)
        sdf.format(Date(recording.timestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = recording.fileName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = recording.transcription.ifBlank { "..." },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { onPlayClick(recording)}) { Text("Play") }
                    Spacer(Modifier.width(16.dp))
                    TextButton(onClick = { onDeleteClick(recording)}) { Text("Delete") }
                }
            }
        }
    }
}

