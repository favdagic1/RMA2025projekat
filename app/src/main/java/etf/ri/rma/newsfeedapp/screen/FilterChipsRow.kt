package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun FilterChipsRow(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf(
        "Sve",
        "Politika",
        "Sport",
        "Nauka/tehnologija",
        "Zabava",
        "Više filtera…"
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = {
                    onCategorySelected(category)
                },
                label = { Text(text = category) },
                modifier = Modifier.testTag(
                    "filter_chip_" + when (category) {
                        "Sve" -> "all"
                        "Politika" -> "pol"
                        "Sport" -> "spo"
                        "Nauka/tehnologija" -> "sci"
                        "Zabava" -> "none"
                        "Više filtera…" -> "more"
                        else -> category.lowercase().replace(" ", "_")
                    }
                )
            )
        }
    }
}
