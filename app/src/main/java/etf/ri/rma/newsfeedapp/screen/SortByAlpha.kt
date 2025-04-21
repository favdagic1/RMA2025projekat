package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

enum class SortOrder {
    NONE, ASC, DESC
}

@Composable
fun SortByAlpha(
    sortOrder: SortOrder,
    onSortChanged: (SortOrder) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(7.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        FilterChip(
            selected = sortOrder == SortOrder.ASC,
            onClick = {
                // aktivira ASC kad nije vec aktiviran, u suprotnom prestani sort.
                if (sortOrder == SortOrder.ASC) onSortChanged(SortOrder.NONE)
                else onSortChanged(SortOrder.ASC)
            },
            label = { Text(text = "Izvor A-Z") },
            modifier = Modifier.testTag("sort_chip_source_asc")
        )
        FilterChip(
            selected = sortOrder == SortOrder.DESC,
            onClick = {
                // aktiviraj DESC ako vec nije aktiviran, u suprotnom prestani .
                if (sortOrder == SortOrder.DESC) onSortChanged(SortOrder.NONE)
                else onSortChanged(SortOrder.DESC)
            },
            label = { Text(text = "Izvor Z-A") },
            modifier = Modifier.testTag("sort_chip_source_desc")
        )
    }
}