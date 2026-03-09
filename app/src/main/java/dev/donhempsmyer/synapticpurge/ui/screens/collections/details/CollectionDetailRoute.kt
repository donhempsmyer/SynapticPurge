package dev.donhempsmyer.synapticpurge.ui.screens.collections.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.donhempsmyer.synapticpurge.ui.components.BottomBarSpec
import dev.donhempsmyer.synapticpurge.ui.components.ScreenChrome
import dev.donhempsmyer.synapticpurge.ui.components.TopAction
import dev.donhempsmyer.synapticpurge.ui.components.TopBarSpec

@Composable
fun CollectionDetailRoute(
    collectionId: Long,
    onNavigateUp: () -> Unit,
    onChrome: (ScreenChrome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val collection by viewModel.collection(collectionId).collectAsStateWithLifecycle(initialValue = null)

    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    // Publish the delete action into the AppRoot top bar
    SideEffect {
        onChrome(
            ScreenChrome(
                topBar = TopBarSpec(
                    actions = listOf(
                        TopAction(
                            contentDescription = "Delete recording",
                            icon = Icons.Filled.Delete,
                            onClick = { showDeleteConfirm = true }
                        )
                    )
                ),
                bottomBar = BottomBarSpec.None
            )
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete recording?") },
            text = { Text("This will remove the recording from the database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteCollectionById(collectionId)
                        onNavigateUp()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (collection == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…")
        }
    } else {

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(collection!!.title, style = MaterialTheme.typography.headlineSmall)
            }
            item {
                Text(collection!!.content)
            }

        }
    }
}