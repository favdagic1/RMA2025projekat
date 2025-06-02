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
    // Dohvat ViewModela
    val vm: NewsViewModel = viewModel()

    // Pretplata na trenutačnu listu vijesti
    val newsList by vm.displayListFlow.collectAsState()

    // Držimo trenutno izabranu kategoriju
    var selectedCategory by remember { mutableStateOf("Sve") }

    // Prvi put prikazujemo sve inicijalne vijesti
    LaunchedEffect(Unit) {
        vm.loadTopStories("Sve")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { cat ->
                selectedCategory = cat
                if (cat == "Više filtera…") {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterCategory", selectedCategory)
                    navController.navigate("filters")
                } else {
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
