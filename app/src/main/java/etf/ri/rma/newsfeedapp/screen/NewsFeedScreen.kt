package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import etf.ri.rma.newsfeedapp.viewmodel.NewsViewModel

@Composable
fun NewsFeedScreen(navController: NavHostController) {
    val vm: NewsViewModel = viewModel()
    val newsList by vm.displayListFlow.collectAsState()
    var selectedCategory by remember { mutableStateOf("Sve") }

    // Pratimo filtere iz SavedStateHandle-a
    val currentEntry = navController.currentBackStackEntry
    val savedStateHandle = currentEntry?.savedStateHandle

    LaunchedEffect(
        savedStateHandle?.get<String>("filterCategory"),
        savedStateHandle?.get<String>("filterStartDate"),
        savedStateHandle?.get<String>("filterEndDate"),
        savedStateHandle?.get<ArrayList<String>>("filterUnwantedWords")
    ) {
        val category = savedStateHandle?.get<String>("filterCategory") ?: "Sve"
        val startDate = savedStateHandle?.get<String>("filterStartDate")
        val endDate = savedStateHandle?.get<String>("filterEndDate")
        val unwantedWords = savedStateHandle?.get<ArrayList<String>>("filterUnwantedWords") ?: arrayListOf()

        // Ažuriraj odabranu kategoriju
        selectedCategory = category

        // Ako je korisnik kliknuo "Primijeni filtere", filtriraj
        if (
            savedStateHandle?.contains("filterCategory") == true ||
            savedStateHandle?.contains("filterStartDate") == true ||
            savedStateHandle?.contains("filterEndDate") == true ||
            savedStateHandle?.contains("filterUnwantedWords") == true
        ) {
            vm.filterStories(category, startDate, endDate, unwantedWords)
        } else {
            // Inicijalno učitaj sve
            vm.loadTopStories(selectedCategory)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { cat ->
                selectedCategory = cat
                if (cat == "Više filtera…") {
                    // Spremi trenutne filtere prije prelaska na ekran za filtere
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterCategory", selectedCategory)
                    navController.navigate("filters")
                } else {
                    // Očisti filtere i učitaj prema izabranoj kategoriji
                    savedStateHandle?.remove<String>("filterStartDate")
                    savedStateHandle?.remove<String>("filterEndDate")
                    savedStateHandle?.remove<ArrayList<String>>("filterUnwantedWords")
                    vm.loadTopStories(cat)
                }
            }
        )

        if (newsList.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            NewsList(
                newsItems = newsList,
                onItemClick = { id ->
                    navController.navigate("details/$id")
                }
            )
        }
    }
}
