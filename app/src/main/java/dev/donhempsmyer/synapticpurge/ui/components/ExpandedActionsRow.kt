package dev.donhempsmyer.synapticpurge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ExpandedActionsRow(
    onDetails: () -> Unit,
    detailsLabel: String = "Details"
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onDetails) { Text(detailsLabel) }
    }
}