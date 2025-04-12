package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow


@Composable
fun FilterChipsRow(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Sve", "Politika", "Sport","Nauka/tehnologija" ,"Zabava" )
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
                    if (category != selectedCategory) {
                        onCategorySelected(category)
                    }
                },
                label = { Text(text = category) },
                modifier = Modifier.testTag("filter_chip_" + when(category) {
                    "Politika" -> "pol"
                    "Sport" -> "spo"
                    "Nauka/tehnologija" -> "sci"
                    "Sve" -> "all"
                    "Zabava" -> "none"
                    else -> category.lowercase()
                })
            )
        }
    }
}