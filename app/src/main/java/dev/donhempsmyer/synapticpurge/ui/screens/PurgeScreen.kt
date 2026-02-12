package dev.donhempsmyer.synapticpurge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlin.text.format



@Composable
fun PurgeScreen(
    isRecording: Boolean,
    recordings: List<Recording>,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Synaptic Purge",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                        RecordingRow(recording = recording)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                initialScale = 0.1f, // Start very small (button sized)
                transformOrigin = TransformOrigin(0.5f, 0.9f), // Pin to bottom center
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
                initialValue = 0.5f, // starting semi-transparent
                targetValue = 1f, //fully opaque
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse // from 1 back to 0.5
                ),
                label = "breathing_alpha"
            )
            val breathingScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
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
                        // 1. THE GLOW (The "Breathing" part)
                        // We use a Canvas or a simple Surface to draw the glow circle
                        Surface(
                            modifier = Modifier
                                .size(200.dp) // The size of the glow area
                                .graphicsLayer(
                                    scaleX = breathingScale * 1.5f, // Make the glow bigger than the text
                                    scaleY = breathingScale * 1.5f,
                                    alpha = breathingAlpha // This makes the GLOW fade, not the text
                                ),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) // Pick a "calming" color here
                        ) { }

                        // 2. THE TEXT (Stays solid white so it's readable)
                        Text(
                            text = "Listening...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.graphicsLayer(
                                // We can give the text a tiny bit of scale too if you like
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
    recording: Recording
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
        onClick = { expanded = !expanded } // Material3 Surface supports clickable overload in newer versions
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
                    TextButton(onClick = { /* TODO: play audio */ }) { Text("Play") }
                    TextButton(onClick = { /* TODO: share */ }) { Text("Share") }
                    TextButton(onClick = { /* TODO: delete */ }) { Text("Delete") }
                }
            }
        }
    }
}

