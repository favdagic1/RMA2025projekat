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
import java.util.ArrayList

@Composable
fun NewsFeedScreen(navController: NavHostController) {
    // 1) definiramo stanja za filtere
    var selectedCategory by rememberSaveable { mutableStateOf("Sve") }
    var appliedStartDate by rememberSaveable { mutableStateOf<String?>(null) }
    var appliedEndDate by rememberSaveable { mutableStateOf<String?>(null) }
    val appliedUnwantedWords = remember { mutableStateListOf<String>() }

    // formateri za datume
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val dataFormatter    = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // svi podaci
    val allNews = remember { NewsData.getAllNews() }

    // 2) čitanje filtera iz SavedStateHandle
    val savedState = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedState) {
        savedState
            ?.get<String>("filterCategory")
            ?.also {
                selectedCategory = it
                savedState.remove<String>("filterCategory")
            }

        val fs = savedState?.get<String>("filterStartDate")
        val fe = savedState?.get<String>("filterEndDate")
        if (!fs.isNullOrEmpty() && !fe.isNullOrEmpty()) {
            appliedStartDate = fs
            appliedEndDate   = fe
            savedState.remove<String>("filterStartDate")
            savedState.remove<String>("filterEndDate")
        }

        savedState
            ?.get<ArrayList<String>>("filterUnwantedWords")
            ?.also {
                appliedUnwantedWords.clear()
                appliedUnwantedWords.addAll(it)
                savedState.remove<ArrayList<String>>("filterUnwantedWords")
            }
    }

    // 3a) filtriranje po kategoriji
    val byCategory = remember(allNews, selectedCategory) {
        if (selectedCategory == "Sve") allNews
        else allNews.filter { it.category == selectedCategory }
    }
    // 3b) filtriranje po datumu
    val byDate = remember(byCategory, appliedStartDate, appliedEndDate) {
        if (appliedStartDate != null && appliedEndDate != null) {
            val start = LocalDate.parse(appliedStartDate, displayFormatter)
            val end   = LocalDate.parse(appliedEndDate,   displayFormatter)
            byCategory.filter {
                val pub = LocalDate.parse(it.publishedDate, dataFormatter)
                !pub.isBefore(start) && !pub.isAfter(end)
            }
        } else byCategory
    }
    // 3c) filtriranje po nepoželjnim riječima
    val fullyFiltered = remember(byDate, appliedUnwantedWords) {
        if (appliedUnwantedWords.isNotEmpty()) {
            byDate.filter { item ->
                appliedUnwantedWords.none { uw ->
                    item.title.contains(uw, ignoreCase = true)
                            || item.snippet.contains(uw, ignoreCase = true)
                }
            }
        } else byDate
    }

    // 4) UI
    Column(modifier = Modifier.fillMaxSize()) {
        // chipovi i navigacija na FilterScreen
        FilterChipsRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { cat ->
                if (cat == "Više filtera…") {
                    navController.navigate("/filters")
                } else {
                    selectedCategory = cat
                }
            }
        )

        // lista vijesti ili poruka
        if (fullyFiltered.isEmpty()) {
            MessageCard(message = "Nema pronađenih vijesti")
        } else {
            NewsList(
                newsItems  = fullyFiltered,
                onItemClick = { id ->
                    navController.navigate("/details/$id")
                }
            )
        }
    }
}
