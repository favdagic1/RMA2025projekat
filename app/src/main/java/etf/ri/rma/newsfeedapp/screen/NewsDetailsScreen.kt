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
    // 1) dohvatimo sve vijesti i pronađemo onu kliknutu
    val allNews: List<NewsItem> = NewsData.getAllNews()
    val current = allNews.find { it.id.toString() == newsId } ?: return

    // formatter za parsedDate iz modela ("yyyy-MM-dd")
    val dataFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val currentDate = LocalDate.parse(current.publishedDate, dataFormatter)

    // 2) izračun najbližih dvije vijesti iste kategorije
    val related: List<NewsItem> = allNews
        .filter { it.category == current.category && it.id != current.id }
        .map { other ->
            val d = LocalDate.parse(other.publishedDate, dataFormatter)
            other to abs(ChronoUnit.DAYS.between(currentDate, d))
        }
        .sortedWith(
            compareBy<Pair<NewsItem, Long>>(
                { it.second },                             // prvo po udaljenosti
                { it.first.publishedDate },                // zatim po datumu (string leksikografski)
                { it.first.title.lowercase(Locale.getDefault()) } // pa po naslovu
            )
        )
        .map { it.first }
        .take(2)

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

        // Povezane vijesti
        Text(
            text = "Povezane vijesti iz iste kategorije:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        related.forEachIndexed { index, item ->
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("/details/${item.id}") }
                    .padding(vertical = 4.dp)
                    .testTag("related_news_title_${index + 1}")
            )
        }
        Spacer(Modifier.height(24.dp))

        // Zatvori detalje
        Button(
            onClick = {
                navController.navigate("/home") {
                    // izbriši sve ekrane iz back stack‑a do /home
                    popUpTo("/home") { inclusive = false }
                    // da ne bi stavilo još jedan /home na vrh
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_close_button")
        )  {
            Text("Zatvori detalje")
        }
    }
}
