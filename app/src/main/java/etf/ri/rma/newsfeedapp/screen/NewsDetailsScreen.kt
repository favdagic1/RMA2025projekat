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
import androidx.compose.ui.Alignment

@Composable
fun NewsDetailsScreen(navController: NavHostController, newsId: String) {
    val vm: NewsViewModel = viewModel()

    // State za loading i error
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State za trenutnu vijest
    var currentNews by remember { mutableStateOf<NewsItem?>(null) }
    val similarList by vm.similarStoriesFlow.collectAsState()
    val imageTags by vm.imageTagsFlow.collectAsState()

    // Pokušaj da pronađe vijest i učita potrebne podatke
    LaunchedEffect(newsId) {
        isLoading = true
        errorMessage = null

        println("DEBUG: Tražim vijest sa ID: $newsId")

        // Prvo pokušaj da pronađe vijest u postojećim podacima
        currentNews = vm.getNewsItemById(newsId)

        println("DEBUG: Pronađena vijest: ${currentNews?.title}")
        println("DEBUG: Ukupno vijesti u DAO: ${vm.getAllStoriesCount()}")

        if (currentNews != null) {
            // Vijest je pronadjena, učitaj dodatne podatke
            vm.loadSimilarStories(newsId)
            currentNews?.imageUrl?.let { vm.loadImageTags(it) }
            isLoading = false
        } else {
            // Vijest nije pronadjena u postojećim podacima
            println("DEBUG: Vijest nije pronađena, pokušavam ponovno...")

            // Sačekaj kratko i pokušaj ponovno (možda se još učitava)
            kotlinx.coroutines.delay(500)
            currentNews = vm.getNewsItemById(newsId)

            if (currentNews != null) {
                vm.loadSimilarStories(newsId)
                currentNews?.imageUrl?.let { vm.loadImageTags(it) }
                isLoading = false
            } else {
                errorMessage = "Vijest nije pronađena. ID: $newsId"
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text("Vrati se na početnu")
                    }
                }
            }

            currentNews != null -> {
                val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
                val current = currentNews!!

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
        }
    }
}