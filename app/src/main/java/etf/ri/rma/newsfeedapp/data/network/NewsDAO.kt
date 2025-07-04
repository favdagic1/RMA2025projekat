package etf.ri.rma.newsfeedapp.data.network

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
    private val API_TOKEN = "g1pNUtBbRf99dRokQ3zRoFfsjcAnIjr3545puKCS"

    init {

        allStories.addAll(
            listOf(
                NewsItem(
                    "b19feda9-099f-41e7-af2c-86fd551a3e2c",
                    "Nova era AI tehnologije",
                    "Umjetna inteligencija ulazi u svakodnevni život.",
                    "https://images.unsplash.com/photo-1465101046530-73398c7f28ca",
                    "general",
                    false,
                    arrayListOf(),
                    "manual",
                    "01-06-2025"
                ),
                NewsItem(
                    "4660b476-d4bb-4f2b-92e4-a7ebd1ada086",
                    "Finalni turnir u Parizu",
                    "Spektakularna završnica nogometnog prvenstva.",
                    "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
                    "sports",
                    false,
                    arrayListOf(),
                    "manual",
                    "02-06-2025"
                ),
                NewsItem(
                    "242f4b59-e4e1-428c-b35d-47ac382995a7",
                    "Nova vlada položila zakletvu",
                    "Političke promjene nakon izbora.",
                    "https://images.unsplash.com/photo-1416339306562-f3d12fefd36f",
                    "politics",
                    false,
                    arrayListOf(),
                    "manual",
                    "03-06-2025"
                ),
                NewsItem(
                    "99820c22-8a4a-498a-b768-957e8930bd73",
                    "Znanstvenici otkrili novi materijal",
                    "Napredak u razvoju održivih tehnologija.",
                    "https://images.unsplash.com/photo-1503676382389-4809596d5290",
                    "science",
                    false,
                    arrayListOf(),
                    "manual",
                    "04-06-2025"
                ),
                NewsItem(
                    "ed7e79b4-8e83-4960-a6ba-8099a9a8485fa",
                    "Analiza tržišta",
                    "Povećanje zaposlenosti i stabilan rast.",
                    "https://images.unsplash.com/photo-1519125323398-675f0ddb6308",
                    "business",
                    false,
                    arrayListOf(),
                    "manual",
                    "05-06-2025"
                ),
                NewsItem(
                    "a85fbb63-4cda-4cea-a2b3-734f1099da9a",
                    "Novi pametni telefon",
                    "Revolucija u mobilnoj industriji.",
                    "https://images.unsplash.com/photo-1454023492550-5696f8ff10e1",
                    "science",
                    false,
                    arrayListOf(),
                    "manual",
                    "06-06-2025"
                ),
                NewsItem(
                    "dc562726-5aad-400c-a18d-b94629abb517",
                    "Filmski festival počinje sutra",
                    "Najbolji filmovi godine u konkurenciji.",
                    "https://images.unsplash.com/photo-1464983953574-0892a716854b",
                    "entertainment",
                    false,
                    arrayListOf(),
                    "manual",
                    "07-06-2025"
                ),
                NewsItem(
                    "9a2f1e00-95ec-4d56-b36a-c16ca1b076cd",
                    "Novi lijek protiv gripe",
                    "Medicinski napredak u prevenciji bolesti.",
                    "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2",
                    "science",
                    false,
                    arrayListOf(),
                    "manual",
                    "08-06-2025"
                ),
                NewsItem(
                    "126cf7a0-e920-427e-b02c-c1853747cefc",
                    "Skriveni dragulji Balkana",
                    "Najljepše destinacije za ljeto 2025.",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
                    "general",
                    false,arrayListOf(),
                    "manual",
                    "09-06-2025"
                ),
                NewsItem(
                    "5dc5754f-90e8-4d22-b0d2-872fcf9384cb",
                    "Ljetni muzički festival",
                    "Poznati izvođači nastupaju uživo.",
                    "https://images.unsplash.com/photo-1470770841072-f978cf4d019e",
                    "entertainment",
                    false,
                    arrayListOf(),
                    "manual",
                    "10-06-2025"
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
                uuid = dto.uuid,
                title = dto.title,
                snippet = dto.snippet ?: "",
                imageUrl = dto.imageUrl,
                category = primaryCat,
                isFeatured = true,
                imageTags = arrayListOf(),
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
            val idx = allStories.indexOfFirst { it.uuid == item.uuid }
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
        val dtoList = response.data.take(2)

        val fetchedItems = dtoList.map { dto ->
            val primaryCat = dto.categories?.firstOrNull() ?: ""
            NewsItem(
                uuid = dto.uuid,
                title = dto.title,
                snippet = dto.snippet ?: "",
                imageUrl = dto.imageUrl,
                category = primaryCat,
                isFeatured = false,
                imageTags = arrayListOf(),
                source = dto.source ?: "",
                publishedDate = dto.publishedAt ?: ""
            )
        }

        fetchedItems.forEach { item ->
            val idx = allStories.indexOfFirst { it.uuid == item.uuid }
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

    suspend fun searchNews(searchString: String): List<NewsItem> {
        val query = searchString
            .replace(" ILI ", " OR ")
            .replace(" I ", " AND ")

        val response = apiService.searchNews(query, API_TOKEN)
        val fetchedItems = response.data.map { dto ->
            val primaryCat = dto.categories?.firstOrNull() ?: ""
            NewsItem(
                uuid = dto.uuid,
                title = dto.title,
                snippet = dto.snippet ?: "",
                imageUrl = dto.imageUrl,
                category = primaryCat,
                isFeatured = false,
                imageTags = arrayListOf(),
                source = dto.source ?: "",
                publishedDate = dto.publishedAt ?: ""
            )
        }

        fetchedItems.forEach { item ->
            val idx = allStories.indexOfFirst { it.uuid == item.uuid }
            if (idx >= 0) {
                val existing = allStories.removeAt(idx)
                allStories.add(0, existing)
            } else {
                allStories.add(0, item)
            }
        }

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
