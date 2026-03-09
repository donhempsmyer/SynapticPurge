package dev.donhempsmyer.synapticpurge.ui.screens.collections


import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.helpers.AudioPlayback
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.TopBarSpec

@Composable
fun CollectionsRoute(
    searchQuery: String,
    onOpenCollection: (Long) -> Unit,
    onChrome: (ScreenChrome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    LaunchedEffect(searchQuery) { viewModel.onQueryChange(searchQuery) }
    val collections by viewModel.collections.collectAsStateWithLifecycle()

    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

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
        selectedIds = collections.map { it.id }.toSet()
    }

    val selectedCount = selectedIds.size

    LaunchedEffect(isSelecting, selectedCount) {
        onChrome(
            if (isSelecting) {
                ScreenChrome(
                    topBar = TopBarSpec(
                        showCancel = true,
                        onCancel = { cancelSelection() }
                    ),
                    bottomBar = BottomBarSpec.Selection(
                        selectedCount = selectedCount,
                        onSelectAll = selectAll,
                        onClear = clearSelection,
                        onPrimary = {
                            viewModel.deleteCollectionsByIds(selectedIds.toList())
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

    CollectionsScreen(
        collections = collections,
        onOpenCollection = onOpenCollection,
        isSelecting = isSelecting,
        selectedIds = selectedIds,
        onCheckboxClick = onCheckboxClick,
        modifier = modifier
    )
}