package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsFeedScreen() {
    var selectedCategory by remember { mutableStateOf("Sve") }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }

    val allNews = remember { NewsData.getAllNews() }

    val filteredNews = if (selectedCategory == "Sve") {
        allNews
    } else {

        allNews.filter { it.category == selectedCategory }
    }

    val sortedNews = when (sortOrder) {
        SortOrder.ASC -> filteredNews.sortedBy { it.source }
        SortOrder.DESC -> filteredNews.sortedByDescending { it.source }
        else -> filteredNews
    }


    Column(modifier = Modifier.fillMaxSize()) {

        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                // Reset sort order when category changes.
                sortOrder = SortOrder.NONE
            }
        )
        // Sorting Row using sort chips defined in SortByAlpha.kt
        SortByAlpha(
            sortOrder = sortOrder,
            onSortChanged = { newSort ->
                sortOrder = newSort
            }
        )
        // Display message if no news are available, otherwise display the list
        if (sortedNews.isEmpty()) {
            MessageCard(message = "Nema pronađenih vijesti u kategoriji $selectedCategory")
        } else {
            NewsList(newsItems = sortedNews)
        }
    }
}