package dev.donhempsmyer.synapticpurge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectionBottomBar(spec: BottomBarSpec.Selection) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TextButton(onClick = spec.onSelectAll) { Text("Select all") }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Button(
                    onClick = spec.onPrimary,
                    enabled = spec.primaryEnabled && spec.selectedCount > 0
                ) { Text("${spec.primaryLabel} (${spec.selectedCount})") }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TextButton(onClick = spec.onClear, enabled = spec.selectedCount > 0) { Text("Clear") }
            }
        }
    }
}