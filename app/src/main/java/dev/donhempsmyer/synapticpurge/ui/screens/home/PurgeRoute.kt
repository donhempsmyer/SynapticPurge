package dev.donhempsmyer.synapticpurge.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayer
import dev.donhempsmyer.synapticpurge.ui.PurgeFabState
import dev.donhempsmyer.synapticpurge.viewModels.PurgeViewModel

@Composable
fun PurgeRoute(
    modifier: Modifier = Modifier,
    viewModel: PurgeViewModel = hiltViewModel(),
    onFabState: (PurgeFabState) -> Unit
) {
    val context = LocalContext.current

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()

    val audioPlayer = remember(context) { AudioPlayer(context) }
    DisposableEffect(Unit) { onDispose { audioPlayer.stop() } }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleRecording()
    }

    val onRecordFabClick = {
        audioPlayer.stop() // optional: prevent overlap

        if (isRecording) {
            viewModel.toggleRecording()
        } else {
            val permission = Manifest.permission.RECORD_AUDIO
            val granted = ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED

            if (granted) viewModel.toggleRecording()
            else requestPermissionLauncher.launch(permission)
        }
    }

    val latestFabClick by rememberUpdatedState(onRecordFabClick)

    SideEffect {
        onFabState(
            PurgeFabState(
                isRecording = isRecording,
                onClick = { latestFabClick() }
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        PurgeScreen(
            isRecording = isRecording,
            recordings = recordings,
            onPlayRecording = { rec -> audioPlayer.play(rec.filePath) },
            onDeleteRecording = { rec -> viewModel.deleteRecording(rec) },
            modifier = Modifier.fillMaxSize()
        )
    }
}


