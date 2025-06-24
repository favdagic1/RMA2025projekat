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
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer

@Composable
fun NewsDetailsScreen(navController: NavHostController, newsId: String) {
    val vm: NewsViewModel = viewModel()


    val allNews by vm.allStoriesFlow.collectAsState()

    val similarList by vm.similarStoriesFlow.collectAsState()

    val imageTags by vm.imageTagsFlow.collectAsState()


    val current = remember(allNews, newsId) {
        allNews.find { it.uuid == newsId }
    }

    if (current == null) {
        Text("Vijest nije pronađena.", modifier = Modifier.padding(16.dp))
        return
    }


    LaunchedEffect(newsId) {
        vm.loadSimilarStories(newsId)
        current.imageUrl?.let { vm.loadImageTags(it) }
    }


    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val curDate = LocalDate.parse(current.publishedDate, formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = current.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_title")
        )
        Spacer(Modifier.height(8.dp))

        if (current.imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(current.imageUrl),
                contentDescription = "Slika vijesti",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        Text(
            text = current.snippet,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_snippet")
        )
        Spacer(Modifier.height(8.dp))


        Text(
            text = current.category,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_category")
        )
        Spacer(Modifier.height(4.dp))


        Text(
            text = current.source,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_source")
        )
        Spacer(Modifier.height(4.dp))


        Text(
            text = current.publishedDate,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_date")
        )
        Spacer(Modifier.height(16.dp))


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
                    Text("- ${tag.value}")

                }
            }
            Spacer(Modifier.height(16.dp))
        }


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
                            navController.navigate("details/${item.uuid}") {
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
