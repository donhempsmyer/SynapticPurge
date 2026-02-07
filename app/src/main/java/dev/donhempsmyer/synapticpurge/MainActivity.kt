package dev.donhempsmyer.synapticpurge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PurgeViewModel by viewModels()


    //Permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.toggleRecording()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {

                val isRecording by viewModel.isRecording

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        // THE RECORD BUTTON
                        Button(
                            onClick = {
                                if (isRecording) {
                                    viewModel.toggleRecording()
                                } else {
                                    val permission = android.Manifest.permission.RECORD_AUDIO
                                    val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        permission
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (isGranted) {
                                        viewModel.toggleRecording()
                                    } else {
                                        //Trigger permission request popup
                                        requestPermissionLauncher.launch(permission)
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(width = 200.dp, height = 80.dp), // Making it "Large" as you requested
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isRecording) "STOP PURGE" else "START PURGE")
                        }
                    },
                    floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
                ) { innerPadding ->
                    PurgeScreen(
                        isRecording = isRecording,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PurgeScreen(isRecording: Boolean, modifier: Modifier = Modifier) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TITLE
            Text(
                text = "Synaptic Purge",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // NOTES LIST (The "Brain Dump" area)
            // Weight(1f) tells the list to take up all available space,
            // pushing the button to the bottom.
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        "Your transcriptions will appear here...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        //Layer 2 for blackout overlay when recording
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
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Column(
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        PurgeScreen(isRecording = false)
    }
}