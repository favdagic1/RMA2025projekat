package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.NewsApiResponse
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class NewsDAO {
    private lateinit var apiService: NewsApiService

    private val allStories = mutableListOf<NewsItem>()
    private val topStoriesCache = mutableMapOf<String, Pair<List<NewsItem>, Long>>()
    private val CACHE_DURATION_MS = 30_000L
    private val similarCache = mutableMapOf<String, List<NewsItem>>()
    private val API_TOKEN = "demo"

    init {
        // (inicijalna 10 vijesti – u svakoj zamijeniti 'id = ...' s 'uuid = ...')
        allStories.addAll(
            listOf(
                NewsItem(
                    uuid = "init-1",
                    title = "Početna vijest Politika 1",
                    snippet = "Izvorna politička vijest 1",
                    imageUrl = null,
                    category = "politics",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "01-03-2025"
                ),
                NewsItem(
                    uuid = "init-2",
                    title = "Početna vijest Sport 1",
                    snippet = "Izvorna sportska vijest 1",
                    imageUrl = null,
                    category = "sports",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "02-03-2025"
                ),
                NewsItem(
                    uuid = "init-3",
                    title = "Početna vijest Nauka 1",
                    snippet = "Izvorna znanstvena vijest 1",
                    imageUrl = null,
                    category = "science",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "03-03-2025"
                ),
                NewsItem(
                    uuid = "init-4",
                    title = "Početna vijest Business 1",
                    snippet = "Izvorna poslovna vijest 1",
                    imageUrl = null,
                    category = "business",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "04-03-2025"
                ),
                NewsItem(
                    uuid = "init-5",
                    title = "Početna vijest Health 1",
                    snippet = "Izvorna zdravstvena vijest 1",
                    imageUrl = null,
                    category = "health",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "05-03-2025"
                ),
                NewsItem(
                    uuid = "init-6",
                    title = "Početna vijest Entertainment 1",
                    snippet = "Izvorna zabavna vijest 1",
                    imageUrl = null,
                    category = "entertainment",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "06-03-2025"
                ),
                NewsItem(
                    uuid = "init-7",
                    title = "Početna vijest Tech 1",
                    snippet = "Izvorna tehnološka vijest 1",
                    imageUrl = null,
                    category = "tech",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "07-03-2025"
                ),
                NewsItem(
                    uuid = "init-8",
                    title = "Početna vijest Politika 2",
                    snippet = "Izvorna politička vijest 2",
                    imageUrl = null,
                    category = "politics",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "08-03-2025"
                ),
                NewsItem(
                    uuid = "init-9",
                    title = "Početna vijest Sport 2",
                    snippet = "Izvorna sportska vijest 2",
                    imageUrl = null,
                    category = "sports",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "09-03-2025"
                ),
                NewsItem(
                    uuid = "init-10",
                    title = "Početna vijest Nauka 2",
                    snippet = "Izvorna znanstvena vijest 2",
                    imageUrl = null,
                    category = "science",
                    isFeatured = false,
                    source = "Example",
                    publishedDate = "10-03-2025"
                )
            )
        )
    }

    fun setApiService(service: NewsApiService) {
        apiService = service
    }

    fun getAllStories(): List<NewsItem> {
        return allStories.toList()
    }

    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> {
        val now = System.currentTimeMillis()
        var lower = category.lowercase()

        val validCategories = setOf(
            "general", "science", "sports", "business",
            "health", "entertainment", "tech", "politics",
            "food", "travel"
        )
        if (!validCategories.contains(lower)) {
            throw IllegalArgumentException("Neispravna kategorija")
        }

        topStoriesCache[lower]?.let { (cachedList, timestamp) ->
            if (now - timestamp < CACHE_DURATION_MS) {
                return cachedList
            }
        }

        val response = apiService.getTopStoriesByCategory(lower, API_TOKEN)
        val dtoList = response.data.take(3)

        val fetchedItems = dtoList.map { dto ->
            val primaryCat = dto.categories?.firstOrNull() ?: ""
            NewsItem(
                uuid = dto.uuid,               // ime parametra je sad `uuid`
                title = dto.title,
                snippet = dto.snippet ?: "",
                imageUrl = dto.imageUrl,
                category = primaryCat,
                isFeatured = true,
                source = dto.source ?: "",
                publishedDate = dto.publishedAt ?: ""
            )
        }

        allStories.forEach { it ->
            if (it.category == lower && it.isFeatured) {
                it.isFeatured = false
            }
        }

        fetchedItems.forEach { item ->
            val idx = allStories.indexOfFirst { it.id == item.id }
            if (idx >= 0) {
                val existing = allStories.removeAt(idx)
                existing.isFeatured = true
                allStories.add(0, existing)
            } else {
                allStories.add(0, item)
            }
        }

        topStoriesCache[lower] = Pair(fetchedItems, now)
        return fetchedItems
    }

    suspend fun getSimilarStories(uuid: String): List<NewsItem> {
        try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Neispravan UUID: $uuid")
        }

        similarCache[uuid]?.let { return it }

        val response = apiService.getSimilarStories(uuid, API_TOKEN)
        val dtoList = response.data

        val fetchedItems = dtoList.map { dto ->
            val primaryCat = dto.categories?.firstOrNull() ?: ""
            NewsItem(
                uuid = dto.uuid,               // opet: parametar je `uuid`
                title = dto.title,
                snippet = dto.snippet ?: "",
                imageUrl = dto.imageUrl,
                category = primaryCat,
                isFeatured = false,
                source = dto.source ?: "",
                publishedDate = dto.publishedAt ?: ""
            )
        }

        fetchedItems.forEach { item ->
            val idx = allStories.indexOfFirst { it.id == item.id }
            if (idx >= 0) {
                val existing = allStories.removeAt(idx)
                allStories.add(0, existing)
            } else {
                allStories.add(0, item)
            }
        }

        similarCache[uuid] = fetchedItems
        return fetchedItems
    }

    companion object {
        fun createWithBaseUrl(baseUrl: String): NewsDAO {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(NewsApiService::class.java)
            val dao = NewsDAO()
            dao.setApiService(service)
            return dao
        }
    }
}
