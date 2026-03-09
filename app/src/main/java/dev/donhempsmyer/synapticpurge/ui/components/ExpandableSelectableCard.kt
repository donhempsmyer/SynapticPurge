package dev.donhempsmyer.synapticpurge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableSelectableCard(
    id: Long,
    isSelecting: Boolean,
    isSelected: Boolean,
    onCheckboxClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 2.dp,
    header: @Composable ColumnScope.() -> Unit,
    collapsedBody: (@Composable ColumnScope.() -> Unit)? = null,
    expandedBody: (@Composable ColumnScope.() -> Unit)? = null
) {
    var expanded by rememberSaveable(id) { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        tonalElevation = tonalElevation,
        shape = MaterialTheme.shapes.medium,
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    header()
                }

                // Checkbox shows when selecting OR expanded
                if (isSelecting || expanded) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { _ -> onCheckboxClick() }
                    )
                } else {
                    Spacer(Modifier.size(60.dp))
                }
            }

            // Optional collapsed body (only when not expanded)
            if (!expanded) {
                collapsedBody?.invoke(this)
            }

            // Expanded body (only when expanded AND not selecting)
            if (expanded && !isSelecting) {
                expandedBody?.let {
                    Spacer(Modifier.height(10.dp))
                    it(this)
                }
            }
        }
    }
}