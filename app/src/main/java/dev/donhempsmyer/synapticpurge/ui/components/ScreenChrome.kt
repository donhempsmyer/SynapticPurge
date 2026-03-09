package dev.donhempsmyer.synapticpurge.ui.components


import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class BottomBarSpec : Parcelable {
    @Parcelize
    data object None : BottomBarSpec()

    @Parcelize
    data class Selection(
        val selectedCount: Int,
        val onSelectAll: () -> Unit,
        val onClear: () -> Unit,
        val onPrimary: () -> Unit,
        val primaryLabel: String,
        val primaryEnabled: Boolean = true
    ) : BottomBarSpec()
}

data class TopBarSpec(
    val showOverflow: Boolean = false,
    val overflowItems: List<OverflowItem> = emptyList(),
    val showCancel: Boolean = false,
    val onCancel: (() -> Unit)? = null,
    val actions: List<TopAction> = emptyList()
)

data class TopAction(
    val contentDescription: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

data class OverflowItem(
    val label: String,
    val onClick: () -> Unit
)

data class ScreenChrome(
    val topBar: TopBarSpec = TopBarSpec(),
    val bottomBar: BottomBarSpec = BottomBarSpec.None
)