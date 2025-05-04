package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import java.util.*

@Composable
fun NewsDetailsScreen(navController: NavHostController, newsId: String) {
    val allNews = NewsData.getAllNews()
    val current = allNews.find { it.id == newsId } ?: return

    // Parsiraj prema `dd-MM-yyyy`
    val dataFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val currentDate   = LocalDate.parse(current.publishedDate, dataFormatter)

    val related = allNews
        .filter { it.category == current.category && it.id != current.id }
        .map { other ->
            val d = LocalDate.parse(other.publishedDate, dataFormatter)
            other to abs(ChronoUnit.DAYS.between(currentDate, d))
        }
        .sortedWith(
            compareBy<Pair<NewsItem, Long>>(
                { it.second },
                { it.first.publishedDate },
                { it.first.title.lowercase(Locale.getDefault()) }
            )
        )
        .map { it.first }
        .take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(current.title,
            style    = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_title")
        )
        Spacer(Modifier.height(8.dp))
        Text(current.snippet,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_snippet")
        )
        Spacer(Modifier.height(8.dp))
        Text(current.category,
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_category")
        )
        Spacer(Modifier.height(4.dp))
        Text(current.source,
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_source")
        )
        Spacer(Modifier.height(4.dp))
        Text(current.publishedDate,
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_date")
        )
        Spacer(Modifier.height(16.dp))
        Text("Povezane vijesti iz iste kategorije:",
            style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        related.forEachIndexed { idx, item ->
            Text(item.title,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("details/${item.id}") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    .padding(vertical = 4.dp)
                    .testTag("related_news_title_${idx+1}")
            )
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
