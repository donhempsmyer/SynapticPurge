package dev.donhempsmyer.synapticpurge.ui


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.donhempsmyer.synapticpurge.ui.screens.home.PurgeRoute
import dev.donhempsmyer.synapticpurge.ui.screens.recordings.CollectionsRoute

// ---------------------------
// Destinations
// ---------------------------
sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    data object Purge : AppDestination(
        route = "purge",
        label = "Purge",
        icon = Icons.Filled.Mic,
        contentDescription = "Purge screen"
    )

    data object Collections : AppDestination(
        route = "collections",
        label = "Collections",
        icon = Icons.Filled.CollectionsBookmark,
        contentDescription = "Purge collections"
    )
}

data class PurgeFabState(
    val isRecording: Boolean,
    val onClick: () -> Unit
)

@Composable
fun PurgeAppRoot(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Root holds the current Purge FAB behavior (provided by PurgeRoute)
    var purgeFabState by remember { mutableStateOf<PurgeFabState?>(null) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            PurgeBottomBar(
                currentRoute = currentRoute,
                purgeFabState = purgeFabState,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Purge.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Purge.route) {
                PurgeRoute(
                    onFabState = { purgeFabState = it }
                )
            }
            composable(AppDestination.Collections.route) {
                CollectionsRoute()
            }
        }
    }
}

// ---------------------------
// Bottom Bar (3-slot symmetry)
// Left: Collections tab
// Center: Purge FAB (only on Purge) else Purge tab item
// Right: reserved placeholder (for future third tab)
// ---------------------------
@Composable
private fun PurgeBottomBar(
    currentRoute: String?,
    purgeFabState: PurgeFabState?,
    onNavigate: (AppDestination) -> Unit
) {
    val onPurge = currentRoute == AppDestination.Purge.route
    val onCollections = currentRoute == AppDestination.Collections.route

    NavigationBar {

        if(!onPurge) {
            NavigationBarItem(
                selected = false,
                onClick = { onNavigate(AppDestination.Purge) },
                icon = { Icon(AppDestination.Purge.icon, AppDestination.Purge.contentDescription) },
                label = { Text(AppDestination.Purge.label) },
                modifier = Modifier.weight(1f)
            )
        }
        // CENTER
        if (onPurge) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                PurgeFabInNavBar(
                    isRecording = purgeFabState?.isRecording ?: false,
                    onClick = purgeFabState?.onClick ?: {}
                )
            }
        }

        NavigationBarItem(
            selected = onCollections,
            onClick = { if (!onCollections) onNavigate(AppDestination.Collections) },
            icon = { Icon(AppDestination.Collections.icon, AppDestination.Collections.contentDescription) },
            label = { Text(AppDestination.Collections.label) },
            modifier = Modifier.weight(1f)
        )

    }
}

@Composable
private fun PurgeFabInNavBar(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isRecording) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        label = "FabColor"
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        icon = {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Start recording"
            )
        },
        text = { Text(if (isRecording) "STOP" else "RECORD") },
        // Optional: helps it visually "nest" into the bar
        modifier = Modifier.padding(vertical = 6.dp)
    )
}