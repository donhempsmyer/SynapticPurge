package dev.donhempsmyer.synapticpurge.ui.screens.home

data class PurgeTopBarActions(
    val isSelecting: Boolean,
    val selectedCount: Int,
    val onConvertToday: () -> Unit,
    val onEnterSelection: () -> Unit,
    val onConvertSelection: () -> Unit,
    val onCancelSelection: () -> Unit,
    val onSelectAll: () -> Unit,
    val onClearSelection: () -> Unit
)