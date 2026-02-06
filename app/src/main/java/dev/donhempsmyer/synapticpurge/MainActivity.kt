package dev.donhempsmyer.synapticpurge

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    //Permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
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

                var isRecording by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        // THE RECORD BUTTON
                        Button(
                            onClick = {
                                if (isRecording) {
                                    isRecording = false
                                } else {
                                    val permission = android.Manifest.permission.RECORD_AUDIO
                                    val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        permission
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (isGranted) {
                                        isRecording = true
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
        if (isRecording) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Recording...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    //Future animation
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