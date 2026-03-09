package dev.donhempsmyer.synapticpurge.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed class PurgeDestinations(val route: String, val label: String, val icon: ImageVector, val searchable: Boolean) {
    object Home : PurgeDestinations("home", "Home", icon = Icons.Filled.Mic, searchable = false)
    object Collections : PurgeDestinations("collections", "Collections", Icons.Filled.CollectionsBookmark, searchable = true)
    object Archive : PurgeDestinations("archive", "Archive", icon = Icons.Filled.Inventory2, searchable = true)
    object Settings : PurgeDestinations("settings", "Settings", icon = Icons.Filled.Settings, searchable = false)

    companion object {
        fun fromRoute(route: String?): PurgeDestinations {
            if (route == null) return Home

            return when {
                route.startsWith("collection/") -> Collections
                route.startsWith("recording/") -> Home
                else -> listOf(Home, Collections, Archive, Settings)
                    .firstOrNull { it.route == route } ?: Home
            }
        }
    }
}