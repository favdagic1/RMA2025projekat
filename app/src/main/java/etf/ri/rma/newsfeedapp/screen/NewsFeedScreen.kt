package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import etf.ri.rma.newsfeedapp.data.NewsData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NewsFeedScreen(navController: NavHostController) {
    // 1) State za glavni filter
    var selectedCategory     by rememberSaveable { mutableStateOf("Sve") }
    var appliedStartDate     by rememberSaveable { mutableStateOf<String?>(null) }
    var appliedEndDate       by rememberSaveable { mutableStateOf<String?>(null) }
    val appliedUnwantedWords = remember { mutableStateListOf<String>() }

    // 2) Format koji svi dijelovi koriste
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    // 3) Uvijek pri kompoziciji "povuci" najnovije state iz FilterScreen-a
    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.let { handle ->
            handle.get<String>("filterCategory")?.let {
                selectedCategory = it
                handle.remove<String>("filterCategory")
            }
            handle.get<String>("filterStartDate")?.let {
                appliedStartDate = it
                handle.remove<String>("filterStartDate")
            }
            handle.get<String>("filterEndDate")?.let {
                appliedEndDate = it
                handle.remove<String>("filterEndDate")
            }
            handle.get<ArrayList<String>>("filterUnwantedWords")?.let { list ->
                appliedUnwantedWords.clear()
                appliedUnwantedWords.addAll(list)
                handle.remove<ArrayList<String>>("filterUnwantedWords")
            }
        }

    // 4) Dohvati i filtriraj vijesti
    val allNews = remember { NewsData.getAllNews() }
    val byCategory = if (selectedCategory == "Sve") allNews
    else allNews.filter { it.category == selectedCategory }
    val byDate = if (appliedStartDate != null && appliedEndDate != null) {
        val start = LocalDate.parse(appliedStartDate, dateFmt)
        val end   = LocalDate.parse(appliedEndDate,   dateFmt)
        byCategory.filter {
            val pub = LocalDate.parse(it.publishedDate, dateFmt)
            !pub.isBefore(start) && !pub.isAfter(end)
        }
    } else byCategory
    val fullyFiltered = if (appliedUnwantedWords.isNotEmpty()) {
        byDate.filter { item ->
            appliedUnwantedWords.none { uw ->
                item.title.contains(uw, ignoreCase = true)
                        || item.snippet.contains(uw, ignoreCase = true)
            }
        }
    } else byDate

    // 5) UI
    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipsRow(
            selectedCategory   = selectedCategory,
            onCategorySelected = { cat ->
                if (cat == "Više filtera…") {
                    // spremi state u handle
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterCategory", selectedCategory)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterStartDate", appliedStartDate)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterEndDate", appliedEndDate)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("filterUnwantedWords", ArrayList(appliedUnwantedWords))
                    // idi na novi ekran
                    navController.navigate("filters")
                } else {
                    selectedCategory = cat
                }
            }
        )

        if (fullyFiltered.isEmpty()) {
            MessageCard(message = "Nema pronađenih vijesti")
        } else {
            NewsList(
                newsItems   = fullyFiltered,
                // **OVDJE**: obavezno bez vodećeg '/'
                onItemClick = { id -> navController.navigate("details/$id") }
            )
        }
    }
}
