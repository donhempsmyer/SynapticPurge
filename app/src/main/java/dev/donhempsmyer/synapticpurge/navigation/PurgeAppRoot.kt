package dev.donhempsmyer.synapticpurge.navigation


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.SelectionBottomBar
import dev.donhempsmyer.synapticpurge.ui.screens.archive.ArchiveRoute
import dev.donhempsmyer.synapticpurge.ui.screens.archive.details.ArchiveDetailsRoute
import dev.donhempsmyer.synapticpurge.ui.screens.home.PurgeFabState
import dev.donhempsmyer.synapticpurge.ui.screens.home.PurgeRoute
import dev.donhempsmyer.synapticpurge.ui.screens.collections.CollectionsRoute
import dev.donhempsmyer.synapticpurge.ui.screens.collections.details.CollectionDetailRoute
import dev.donhempsmyer.synapticpurge.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurgeAppRoot(modifier: Modifier = Modifier) {

    val drawerDestinations = listOf(
        PurgeDestinations.Home,
        PurgeDestinations.Collections,
        PurgeDestinations.Archive,
        PurgeDestinations.Settings
    )

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // FAB state published by PurgeRoute
    var purgeFabState by remember { mutableStateOf<PurgeFabState?>(null) }

    // NEW: unified chrome state (published by whichever Route is active)
    var chrome by remember { mutableStateOf(ScreenChrome()) }

    // Route tracking (destination.route is usually the PATTERN, e.g. "collection/{id}")
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: PurgeDestinations.Home.route

    val isDetails = currentRoute == "recording/{id}" || currentRoute == "collection/{id}"

    val currentDestination = remember(currentRoute) {
        when (currentRoute) {
            "collection/{id}" -> PurgeDestinations.Collections
            "recording/{id}" -> PurgeDestinations.Archive
            else -> PurgeDestinations.fromRoute(currentRoute)
        }
    }

    // ----- Search state owned by AppRoot -----
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus when opening search
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
            delay(50)
            keyboardController?.show()
        }
    }

    // Reset search whenever route changes
    LaunchedEffect(currentRoute) {
        isSearchActive = false
        searchQuery = ""
        chrome = ScreenChrome() // avoid stale screen chrome
    }

    // Never allow search mode on details
    LaunchedEffect(isDetails) {
        if (isDetails) {
            isSearchActive = false
            searchQuery = ""
        }
    }

    // Clear stale FAB state when leaving Home
    LaunchedEffect(currentDestination.route) {
        if (currentDestination.route != PurgeDestinations.Home.route) {
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
                    val selected = currentDestination.route == destination.route
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            snackbarHost = { SnackbarHost(snackbarHostState) },

            topBar = {
                var overflowExpanded by remember { mutableStateOf(false) }

                CenterAlignedTopAppBar(
                    title = {
                        if (isSearchActive && currentDestination.searchable && !isDetails) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search ${currentDestination.label}…") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                shape = MaterialTheme.shapes.extraLarge,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {}),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        } else {
                            Text(currentDestination.label)
                        }
                    },

                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when {
                                    isDetails -> navController.navigateUp()
                                    isSearchActive -> {
                                        isSearchActive = false
                                        searchQuery = ""
                                    }
                                    else -> scope.launch { drawerState.open() }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isDetails || isSearchActive)
                                    Icons.AutoMirrored.Filled.ArrowBack
                                else
                                    Icons.Filled.Menu,
                                contentDescription = when {
                                    isDetails -> "Go back"
                                    isSearchActive -> "Close search"
                                    else -> "Open menu"
                                }
                            )
                        }
                    },

                    actions = {
                        // 1) Cancel (generic, any screen)
                        if (chrome.topBar.showCancel && chrome.topBar.onCancel != null) {
                            TextButton(onClick = chrome.topBar.onCancel!!) {
                                Text("Cancel")
                            }
                        }

                        chrome.topBar.actions.forEach { action ->
                            IconButton(onClick = action.onClick) {
                                Icon(action.icon, contentDescription = action.contentDescription)
                            }
                        }

                        // 2) Overflow (generic, any screen)
                        if (chrome.topBar.showOverflow && chrome.topBar.overflowItems.isNotEmpty()) {
                            IconButton(onClick = { overflowExpanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = overflowExpanded,
                                onDismissRequest = { overflowExpanded = false }
                            ) {
                                chrome.topBar.overflowItems.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.label) },
                                        onClick = {
                                            overflowExpanded = false
                                            item.onClick()
                                        }
                                    )
                                }
                            }
                        }

                        // 3) Search icon (AppRoot-owned, based on destination flags)
                        if (!isDetails && currentDestination.searchable && !isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        } else if (isSearchActive) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                )
            },

            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                val selecting = (chrome.bottomBar is BottomBarSpec.Selection)
                if (
                    currentDestination.route == PurgeDestinations.Home.route &&
                    purgeFabState != null &&
                    !selecting
                ) {
                    PurgeFab(
                        isRecording = purgeFabState!!.isRecording,
                        onClick = purgeFabState!!.onClick
                    )
                }
            },

            bottomBar = {
                when (val b = chrome.bottomBar) {
                    is BottomBarSpec.Selection -> SelectionBottomBar(b)
                    BottomBarSpec.None -> Unit
                }
            }

        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = PurgeDestinations.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(PurgeDestinations.Home.route) {
                    PurgeRoute(
                        onFabState = { purgeFabState = it },
                        onChrome = { chrome = it },
                        onOpenCollection = { id -> navController.navigate("collection/$id") }
                    )
                }

                composable(PurgeDestinations.Archive.route) {
                    ArchiveRoute(
                        searchQuery = searchQuery,
                        onOpenRecording = { id -> navController.navigate("recording/$id") },
                        onChrome = { chrome = it }
                    )
                }

                composable(PurgeDestinations.Collections.route) {
                    CollectionsRoute(
                        searchQuery = searchQuery,
                        onOpenCollection = { id -> navController.navigate("collection/$id") },
                        onChrome = { chrome = it }
                    )
                }

                composable(PurgeDestinations.Settings.route) {
                    SettingsRoute()
                }

                composable("recording/{id}") { entry ->
                    val id = entry.arguments?.getString("id")?.toLongOrNull()
                    if (id != null) {
                        ArchiveDetailsRoute(
                            recordingId = id,
                            onNavigateUp = { navController.navigateUp() },
                            onChrome = { chrome = it }
                        )
                    }
                }

                composable("collection/{id}") { entry ->
                    val id = entry.arguments?.getString("id")?.toLongOrNull()
                    if (id != null) {
                        CollectionDetailRoute(
                            collectionId = id,
                            onNavigateUp = { navController.navigateUp() },
                            onChrome = { chrome = it }
                        )
                    }
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
        targetValue = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        label = "FabColor"
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
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



