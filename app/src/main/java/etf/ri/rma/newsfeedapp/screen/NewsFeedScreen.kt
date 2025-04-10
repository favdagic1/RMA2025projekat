package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsFeedScreen() {
    var selectedCategory by remember { mutableStateOf("Sve") }
    // Dohvati sve vijesti
    val allNews = remember { NewsData.getAllNews() }
    // Filtriraj vijesti prema odabranoj kategoriji. Ako je "Sve", prikazuje se kompletna lista.
    val filteredNews = if (selectedCategory == "Sve") {
        allNews
    } else {
        allNews.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Prikaz filter čipova
        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        // Ako nema vijesti za odabranu kategoriju, prikaži MessageCard; inače prikaži NewsList
        if(filteredNews.isEmpty()) {
            MessageCard(message = "Nema pronađenih vijesti u kategoriji $selectedCategory")
        } else {
            NewsList(newsItems = filteredNews)
        }
    }
}
