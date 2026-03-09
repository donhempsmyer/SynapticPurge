package dev.donhempsmyer.synapticpurge.ui.screens.archive


import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.TopBarSpec

@Composable
fun ArchiveRoute(
    searchQuery: String,
    onOpenRecording: (Long) -> Unit,
    onChrome: (ScreenChrome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel()
) {


    val recordings by viewModel.recordings.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val audioPlayback = remember { AudioPlayback(scope) }

    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    val enterSelection = {
        isSelecting = true
        selectedIds = emptySet()
    }
    val clearSelection = { selectedIds = emptySet() }
    val cancelSelection = {
        isSelecting = false
        selectedIds = emptySet()
    }

    val onCheckboxClick: (Long) -> Unit = { id ->
        if (!isSelecting) isSelecting = true
        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
    }

    val selectAll = {
        if (!isSelecting) isSelecting = true
        selectedIds = recordings.map { it.id }.toSet()
    }

    DisposableEffect(Unit) {
        onDispose { audioPlayback.stop() }
    }

    LaunchedEffect(searchQuery) {
        viewModel.onQueryChange(searchQuery)
    }

    LaunchedEffect(isSelecting) {
        if (isSelecting) audioPlayback.stop()
    }

    LaunchedEffect(isSelecting, selectedIds.size) {
        onChrome(
            if (isSelecting) {
                ScreenChrome(
                    topBar = TopBarSpec(
                        showCancel = true,
                        onCancel = { cancelSelection() }
                    ),
                    bottomBar = BottomBarSpec.Selection(
                        selectedCount = selectedIds.size,
                        onSelectAll = selectAll,
                        onClear = clearSelection,
                        onPrimary = {
                            viewModel.deleteRecordingsByIds(selectedIds.toList())
                            cancelSelection()
                        },
                        primaryLabel = "Delete",
                        primaryEnabled = true
                    )
                )
            } else {
                ScreenChrome(bottomBar = BottomBarSpec.None)
            }
        )
    }

    ArchiveScreen(
        recordings = recordings,
        onOpenRecording = onOpenRecording,
        audioPlayback = audioPlayback,
        isSelecting = isSelecting,
        selectedIds = selectedIds,
        onCheckboxClick = onCheckboxClick,
        modifier = modifier
    )
}