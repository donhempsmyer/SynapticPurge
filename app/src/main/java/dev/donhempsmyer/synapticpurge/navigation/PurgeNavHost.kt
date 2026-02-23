package dev.donhempsmyer.synapticpurge.navigation


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import dev.donhempsmyer.synapticpurge.ui.screens.home.PurgeRoute
import dev.donhempsmyer.synapticpurge.ui.screens.archive.ArchiveRoute
import dev.donhempsmyer.synapticpurge.ui.screens.home.PurgeFabState
import dev.donhempsmyer.synapticpurge.ui.screens.recordings.CollectionsRoute
import dev.donhempsmyer.synapticpurge.ui.screens.settings.SettingsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurgeAppRoot(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: PurgeDestinations.Purge.route

    // Drawer state + coroutine scope for open/close
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Global snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    // Root FAB state (published by PurgeRoute)
    var purgeFabState by remember { mutableStateOf<PurgeFabState?>(null) }

    // Clear stale FAB state when leaving Purge
    LaunchedEffect(currentRoute) {
        if (currentRoute != PurgeDestinations.Purge.route) {
            purgeFabState = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Synaptic Purge",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(Modifier.height(8.dp))

                drawerDestinations.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = selected,
                        onClick = {
                            // Navigate + close drawer (Option A)
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(routeTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                // Root FAB only on Purge route
                if (currentRoute == PurgeDestinations.Purge.route && purgeFabState != null) {
                    PurgeFab(
                        isRecording = purgeFabState!!.isRecording,
                        onClick = purgeFabState!!.onClick
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = PurgeDestinations.Purge.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(PurgeDestinations.Purge.route) {
                    PurgeRoute(
                        onFabState = { purgeFabState = it }
                    )
                }
                composable(PurgeDestinations.Collections.route) {
                    CollectionsRoute() // can be stub for now
                }
                composable(PurgeDestinations.Archive.route) {
                    ArchiveRoute() // can be stub for now
                }
                composable(PurgeDestinations.Settings.route) {
                    SettingsRoute() // can be stub for now
                }

            }
        }
    }
}

@Composable
private fun PurgeFab(
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
        text = { Text(if (isRecording) "STOP" else "RECORD") }
    )
}

private fun routeTitle(route: String): String = when (route) {
    PurgeDestinations.Purge.route -> "Synaptic Purge"
    PurgeDestinations.Collections.route -> "Collections"
    PurgeDestinations.Archive.route -> "Archive"
    PurgeDestinations.Settings.route -> "Settings"
    else -> "Synaptic Purge"
}