package dev.donhempsmyer.synapticpurge.ui.screens.collections

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.time.LocalDate
import dev.donhempsmyer.synapticpurge.data.collections.Collection
import dev.donhempsmyer.synapticpurge.ui.components.CenteredSectionHeader
import dev.donhempsmyer.synapticpurge.ui.components.formatSectionHeader
import java.time.Instant
import java.time.ZoneId

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import dev.donhempsmyer.synapticpurge.ui.components.ExpandableSelectableCard

@Composable
fun CollectionsScreen(
    collections: List<Collection>,
    onOpenCollection: (Long) -> Unit,
    isSelecting: Boolean,
    selectedIds: Set<Long>,
    onCheckboxClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }

    val grouped = remember(collections) {
        collections.groupBy { c ->
            Instant.ofEpochMilli(c.createdAt).atZone(zone).toLocalDate()
        }
    }

    val sectionDates = remember(grouped, today) {
        val dates = grouped.keys.sortedDescending()
        if (dates.contains(today)) listOf(today) + dates.filter { it != today } else dates
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        sectionDates.forEach { date ->
            stickyHeader(key = "collections_header_$date") {
                CenteredSectionHeader(text = formatSectionHeader(date, today))
            }

            val dayItems = grouped[date].orEmpty()

            items(dayItems, key = { it.id }) { c ->
                CollectionRow(
                    collection = c,
                    isSelecting = isSelecting,
                    isSelected = selectedIds.contains(c.id),
                    onCheckboxClick = { onCheckboxClick(c.id) },
                    onOpenDetails = { onOpenCollection(c.id) }
                )

                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CollectionRow(
    collection: Collection,
    isSelecting: Boolean,
    isSelected: Boolean,
    onCheckboxClick: () -> Unit,
    onOpenDetails: () -> Unit
) {
    ExpandableSelectableCard(
        id = collection.id,
        isSelecting = isSelecting,
        isSelected = isSelected,
        onCheckboxClick = onCheckboxClick,
        header = {
            Text(collection.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                collection.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        },
        expandedBody = {
            // Only shown when expanded AND not selecting (your rule)
            Text(
                collection.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onOpenDetails) { Text("Details") }
            }

            // Optional future: if Collection ever stores audioPath
            // AudioPlayer(playback = audioPlayback, filePath = collection.audioPath, ...)
        }
    )
}
