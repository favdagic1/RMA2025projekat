package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.viewmodel.NewsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import java.util.*

@Composable
fun NewsDetailsScreen(navController: NavHostController, newsId: String) {
    val vm: NewsViewModel = viewModel()

    // 1) Pratimo sve vijesti u memoriji
    val allNews by vm.allStoriesFlow.collectAsState()
    // 2) Pratimo slične vijesti
    val similarList by vm.similarStoriesFlow.collectAsState()
    // 3) Pratimo tagove za sliku
    val imageTags by vm.imageTagsFlow.collectAsState()

    // 4) Pronađemo odabranu vijest
    val current = remember(allNews, newsId) {
        allNews.find { it.id == newsId }
    } ?: return

    // 5) Pozivamo DAO-e (slične vijesti i tagove) odmah pri otvaranju
    LaunchedEffect(newsId) {
        vm.loadSimilarStories(newsId)
        current.imageUrl?.let { vm.loadImageTags(it) }
    }

    // 6) Parsiramo datum
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val curDate = LocalDate.parse(current.publishedDate, formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Naslov
        Text(
            text = current.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_title")
        )
        Spacer(Modifier.height(8.dp))

        // Sažetak
        Text(
            text = current.snippet,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_snippet")
        )
        Spacer(Modifier.height(8.dp))

        // Kategorija
        Text(
            text = current.category,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_category")
        )
        Spacer(Modifier.height(4.dp))

        // Izvor
        Text(
            text = current.source,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_source")
        )
        Spacer(Modifier.height(4.dp))

        // Datum
        Text(
            text = current.publishedDate,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_date")
        )
        Spacer(Modifier.height(16.dp))

        // Tagovi za sliku (ako postoji URL)
        if (current.imageUrl != null) {
            Text(
                text = "Tagovi za sliku:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            if (imageTags.isEmpty()) {
                Text("Nema tagova ili se učitavaju...")
            } else {
                imageTags.forEach { tag ->
                    Text("- $tag")
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Slične vijesti
        Text(
            text = "Slične vijesti:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        if (similarList.isEmpty()) {
            Text("Nema sličnih vijesti ili se učitavaju...")
        } else {
            similarList.forEachIndexed { idx, item ->
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("details/${item.id}") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        .padding(vertical = 4.dp)
                        .testTag("related_news_title_${idx + 1}")
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
    }
}
