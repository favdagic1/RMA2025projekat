package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import etf.ri.rma.newsfeedapp.viewmodel.NewsViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment

@Composable
fun NewsFeedScreen(navController: NavHostController) {
    val vm: NewsViewModel = viewModel()
    val newsList by vm.displayListFlow.collectAsState()
    val errorMessage by vm.errorFlow.collectAsState()
    var selectedCategory by remember { mutableStateOf("Sve") }
    var searchQuery by remember { mutableStateOf("") }

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
                    savedStateHandle?.set("filterCategory", cat)

                    vm.loadTopStories(cat)
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("Pretraga") }
            )
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        vm.searchNews(searchQuery)
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = searchQuery.isNotBlank()
            ) {
                Text("Traži")
            }
        }

        // Prikaz error poruke ako postoji
        errorMessage?.let { error ->
            androidx.compose.material3.Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    androidx.compose.material3.TextButton(onClick = { /* Dismissed */ }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }

        if (newsList.isEmpty() && errorMessage == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Učitavanje vijesti...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (newsList.isEmpty() && errorMessage != null) {
            MessageCard("Nema dostupnih vijesti")
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
