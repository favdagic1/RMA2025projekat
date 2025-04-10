package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp

@Composable
fun NewsList(newsItems: List<etf.ri.rma.newsfeedapp.model.NewsItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(newsItems) { item ->
            if(item.isFeatured)
                FeaturedNewsCard(newsItem = item)
            else
                StandardNewsCard(newsItem = item)
        }
    }
}
