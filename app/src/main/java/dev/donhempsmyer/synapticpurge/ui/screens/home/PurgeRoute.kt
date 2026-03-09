package dev.donhempsmyer.synapticpurge.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.OverflowItem
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.TopBarSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurgeRoute(
    modifier: Modifier = Modifier,
    viewModel: PurgeViewModel = hiltViewModel(),
    onFabState: (PurgeFabState) -> Unit,
    onChrome: (ScreenChrome) -> Unit,
    onOpenCollection: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()

    val audioPlayback = remember { AudioPlayback(scope) }

    DisposableEffect(Unit) {
        onDispose { audioPlayback.stop() }
    }


    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    val clearSelection = { selectedIds = emptySet() }


    val enterSelection = {
        isSelecting = true
        selectedIds = emptySet()
    }

    val cancelSelection = {
        isSelecting = false
        selectedIds = emptySet()
    }

    // Checkbox click: enters selection mode on first use
    val onCheckboxClick: (Long) -> Unit = { id ->
        if (!isSelecting) isSelecting = true
        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
    }

    val selectAll = {
        if (!isSelecting) isSelecting = true
        selectedIds = recordings.map { it.id }.toSet()
    }

    // -----------------------------
    // Recording FAB logic (existing)
    // -----------------------------
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleRecording()
    }

    val onRecordFabClick = {
        audioPlayback.stop()

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
        onFabState(PurgeFabState(isRecording = isRecording, onClick = { latestFabClick() }))
    }

    // -----------------------------
    // Step 3: Bottom sheet state machine
    // -----------------------------
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetStep by rememberSaveable { mutableStateOf<ConvertSheetStep>(ConvertSheetStep.Hidden) }

    // Options UI state
    var convertMode by rememberSaveable { mutableStateOf(ConvertMode.CONDENSE) }
    var customPrompt by rememberSaveable { mutableStateOf("") }

    // Success UI state
    var titleInput by rememberSaveable { mutableStateOf("") }
    var deleteOriginalAudio by rememberSaveable { mutableStateOf(false) }
    var deleteOriginalTranscription by rememberSaveable { mutableStateOf(false) }

    fun resetSheetUi() {
        convertMode = ConvertMode.CONDENSE
        customPrompt = ""
        titleInput = ""
        deleteOriginalAudio = false
        deleteOriginalTranscription = false
    }

    fun openOptionsToday() {
        resetSheetUi()
        sheetStep = ConvertSheetStep.Options(source = ConvertSource.TODAY)
        scope.launch { sheetState.show() }
    }

    fun openOptionsSelected() {
        resetSheetUi()
        val snapshot = selectedIds.toList()
        sheetStep = ConvertSheetStep.Options(source = ConvertSource.SELECTED, selectedIdsSnapshot = snapshot)
        scope.launch { sheetState.show() }
    }

    suspend fun dismissSheet() {
        sheetState.hide()
        sheetStep = ConvertSheetStep.Hidden
        resetSheetUi()
    }


    // Publish unified chrome (top bar + bottom bar)
    val selectedCount = selectedIds.size

    LaunchedEffect(isSelecting, selectedCount) {
        if (!isSelecting) {
            // Normal mode: overflow menu only
            onChrome(
                ScreenChrome(
                    topBar = TopBarSpec(
                        showOverflow = true,
                        overflowItems = listOf(
                            OverflowItem(
                                label = "Convert Today",
                                onClick = { openOptionsToday() }
                            ),
                            OverflowItem(
                                label = "Select Recordings",
                                onClick = { enterSelection() }
                            )
                        ),
                        showCancel = false,
                        onCancel = null
                    ),
                    bottomBar = BottomBarSpec.None
                )
            )
        } else {
            // Selecting mode: cancel button + bottom selection bar
            onChrome(
                ScreenChrome(
                    topBar = TopBarSpec(
                        showOverflow = false,
                        overflowItems = emptyList(),
                        showCancel = true,
                        onCancel = { cancelSelection() }
                    ),
                    bottomBar = BottomBarSpec.Selection(
                        selectedCount = selectedCount,
                        onSelectAll = selectAll,
                        onClear = clearSelection,
                        onPrimary = { openOptionsSelected() }, // opens sheet for selected snapshot
                        primaryLabel = "Convert",
                        primaryEnabled = true
                    )
                )
            )
        }
    }


    LaunchedEffect(isSelecting) {
        if (isSelecting) audioPlayback.stop()
    }


    // -----------------------------
    // Screen + Sheet UI
    // -----------------------------
    Box(modifier = modifier.fillMaxSize()) {

        PurgeScreen(
            isRecording = isRecording,
            recordings = recordings,
            isSelecting = isSelecting,
            selectedIds = selectedIds,
            onCheckboxClick = onCheckboxClick,
            audioPlayback = audioPlayback,
            modifier = Modifier.fillMaxSize()
        )

        var lastConvertSource by rememberSaveable { mutableStateOf(ConvertSource.TODAY) }
        var lastSelectedSnapshot by rememberSaveable { mutableStateOf(listOf<Long>()) }
        var pendingToggle by rememberSaveable { mutableStateOf<String?>(null) }


        if (sheetStep !is ConvertSheetStep.Hidden) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    // Allow dismiss unless actively processing (Step 4 can refine this)
                    if (sheetStep !is ConvertSheetStep.Processing) {
                        scope.launch { dismissSheet() }
                    }
                }
            ) {
                when (val step = sheetStep) {
                    is ConvertSheetStep.Options -> {
                        ConvertSheetOptions(
                            source = step.source,
                            selectedCount = step.selectedIdsSnapshot.size,
                            mode = convertMode,
                            onModeChange = { convertMode = it },
                            customPrompt = customPrompt,
                            onPromptChange = { customPrompt = it },
                            onCancel = { scope.launch { dismissSheet() } },
                            onProcess = {
                                if (convertMode == ConvertMode.CUSTOM && customPrompt.isBlank()) return@ConvertSheetOptions

                                lastConvertSource = step.source
                                lastSelectedSnapshot = step.selectedIdsSnapshot

                                sheetStep = ConvertSheetStep.Processing(
                                    source = step.source,
                                    selectedIdsSnapshot = step.selectedIdsSnapshot,
                                    mode = convertMode,
                                    customPrompt = customPrompt.trim(),
                                    attempt = 0
                                )

                                scope.launch {
                                    try {
                                        val result = viewModel.runConversion(
                                            source = step.source,
                                            selectedIds = step.selectedIdsSnapshot,
                                            mode = convertMode,
                                            customPrompt = customPrompt
                                        )

                                        sheetStep = ConvertSheetStep.Success(
                                            createdCollectionId = 0L,
                                            processedRecordingIds = result.processedIds,
                                            resultText = result.resultText
                                        )
                                    } catch (e: Exception) {
                                        sheetStep = ConvertSheetStep.Success(
                                            createdCollectionId = 0L,
                                            processedRecordingIds = emptyList(),
                                            resultText = "Error: ${e.localizedMessage ?: "Unknown error"}"
                                        )
                                    }
                                }
                            }
                        )
                    }

                    is ConvertSheetStep.Processing -> {
                        ConvertSheetProcessing()
                    }

                    is ConvertSheetStep.Success -> {
                        ConvertSheetSuccess(
                            title = titleInput,
                            onTitleChange = { titleInput = it },
                            deleteAudio = deleteOriginalAudio,
                            onDeleteAudioChange = { checked ->
                                if (checked) pendingToggle = "audio" else deleteOriginalAudio = false
                            },
                            deleteTranscription = deleteOriginalTranscription,
                            onDeleteTranscriptionChange = { checked ->
                                if (checked) pendingToggle = "transcription" else deleteOriginalTranscription = false
                            },
                            onSubmit = {
                                if (titleInput.isBlank()) return@ConvertSheetSuccess

                                val success = sheetStep as? ConvertSheetStep.Success ?: return@ConvertSheetSuccess
                                val idsToAffect = success.processedRecordingIds

                                scope.launch {
                                    val collectionId = viewModel.insertCollection(
                                        title = titleInput,
                                        content = success.resultText,
                                        mode = convertMode,
                                        prompt = if (convertMode == ConvertMode.CUSTOM) customPrompt.trim() else null
                                    )
                                    viewModel.applyPostConvertActions(
                                        ids = idsToAffect,
                                        deleteAudio = deleteOriginalAudio,
                                        deleteTranscription = deleteOriginalTranscription
                                    )

                                    viewModel.hideFromPurgeAfterConvert(
                                        source = lastConvertSource,
                                        selectedIdsSnapshot = lastSelectedSnapshot
                                    )


                                    cancelSelection()
                                    dismissSheet()

                                    onOpenCollection(collectionId)
                                }
                            }
                        )
                    }

                    else -> Unit
                }

                Spacer(Modifier.height(16.dp))
            }
            if (pendingToggle != null) {
                val isAudio = pendingToggle == "audio"
                AlertDialog(
                    onDismissRequest = { pendingToggle = null },
                    title = { Text(if (isAudio) "Delete original audio?" else "Delete original transcription?") },
                    text = {
                        Text(
                            if (isAudio) "After submitting, the audio file will be deleted. This can’t be undone."
                            else "After submitting, the transcription will be cleared. This can’t be undone."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (isAudio) deleteOriginalAudio = true else deleteOriginalTranscription = true
                            pendingToggle = null
                        }) { Text("Confirm") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingToggle = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}


