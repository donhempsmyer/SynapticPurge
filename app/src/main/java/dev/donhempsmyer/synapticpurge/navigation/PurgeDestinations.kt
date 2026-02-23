package dev.donhempsmyer.synapticpurge.navigation

import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings

sealed class PurgeDestinations(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Purge : PurgeDestinations(
        route = "purge",
        label = "Purge",
        icon = androidx.compose.material.icons.Icons.Filled.Mic
    )

    data object Collections : PurgeDestinations(
        route = "collections",
        label = "Collections",
        icon = androidx.compose.material.icons.Icons.Filled.CollectionsBookmark
    )

    data object Archive : PurgeDestinations(
        route = "archive",
        label = "Archive",
        icon = androidx.compose.material.icons.Icons.Filled.Inventory2
    )

    data object Settings : PurgeDestinations(
        route = "settings",
        label = "Settings",
        icon = androidx.compose.material.icons.Icons.Filled.Settings
    )

}

val drawerDestinations = listOf(
    PurgeDestinations.Purge,
    PurgeDestinations.Collections,
    PurgeDestinations.Archive,
    PurgeDestinations.Settings,
)