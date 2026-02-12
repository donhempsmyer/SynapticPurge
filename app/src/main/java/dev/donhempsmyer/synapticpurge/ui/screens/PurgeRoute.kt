package dev.donhempsmyer.synapticpurge.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.viewModels.PurgeViewModel

@Composable
fun PurgeRoute(
    modifier: Modifier = Modifier,
    viewModel: PurgeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "FabColor"
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleRecording()
    }

    val onRecordFabClick = {
        if (isRecording) {
            viewModel.toggleRecording()
        } else {
            val permission = Manifest.permission.RECORD_AUDIO
            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.toggleRecording()
            else requestPermissionLauncher.launch(permission)
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRecordFabClick,
                containerColor = animatedContainerColor,
                icon = {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Start recording"
                    )
                },
                text = { Text(if (isRecording) "STOP PURGE" else "START PURGE") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        PurgeScreen(
            isRecording = isRecording,
            recordings = recordings,
            modifier = Modifier.padding(innerPadding)
        )
    }
}


