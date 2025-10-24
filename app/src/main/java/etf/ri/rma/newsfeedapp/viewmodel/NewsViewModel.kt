package etf.ri.rma.newsfeedapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NewsViewModel : ViewModel() {

    init {
        Logger.d("NewsViewModel kreiran", "NewsViewModel")
    }

    // HTTP client sa timeoutima
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 1) Retrofit i DAO-e
    private val newsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.thenewsapi.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val imaggaRetrofit = Retrofit.Builder()
        .baseUrl("https://api.imagga.com/v2/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val newsApiService: NewsApiService = newsRetrofit.create(NewsApiService::class.java)
    private val imaggaApiService: ImagaApiService = imaggaRetrofit.create(ImagaApiService::class.java)

    private val newsDAO = NewsDAO.createWithBaseUrl("https://api.thenewsapi.com")
    private val imaggaDAO = ImagaDAO().apply { setApiService(imaggaApiService) }

    // 2) Flow za sve vijesti
    private val _allStoriesFlow = MutableStateFlow<List<NewsItem>>(newsDAO.getAllStories())
    val allStoriesFlow: StateFlow<List<NewsItem>> = _allStoriesFlow

    // 3) Flow za trenutačno prikazanu listu
    private val _displayListFlow = MutableStateFlow<List<NewsItem>>(newsDAO.getAllStories())
    val displayListFlow: StateFlow<List<NewsItem>> = _displayListFlow

    // 4) Flow za slične vijesti na detaljnom ekranu
    private val _similarStoriesFlow = MutableStateFlow<List<NewsItem>>(emptyList())
    val similarStoriesFlow: StateFlow<List<NewsItem>> = _similarStoriesFlow

    // 5) Flow za tagove slike
    private val _imageTagsFlow = MutableStateFlow<List<etf.ri.rma.newsfeedapp.data.ImaggaTag>>(emptyList())
    val imageTagsFlow: StateFlow<List<etf.ri.rma.newsfeedapp.data.ImaggaTag>> = _imageTagsFlow

    // 6) Flow za error poruke
    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow

    // 7) Mapiranje bosanski → engleski ključevi
    private val categoryMap = mapOf(
        "Sve" to "all",
        "Politika" to "politics",
        "Sport" to "sports",
        "Nauka/tehnologija" to "science"
    )

    /** Dodana funkcija za pronalaženje vijesti po UUID-u */
    fun getNewsItemById(uuid: String): NewsItem? {
        val found = newsDAO.findStoryByUuid(uuid)
        Logger.d("Tražim UUID: $uuid, pronašao: ${found?.title}", "NewsViewModel")
        return found
    }

    /** Debug funkcija za brojanje vijesti */
    fun getAllStoriesCount(): Int {
        val count = newsDAO.getAllStories().size
        Logger.d("Ukupno vijesti u DAO: $count", "NewsViewModel")
        newsDAO.getAllStories().forEach {
            Logger.d("- ${it.uuid}: ${it.title}", "NewsViewModel")
        }
        return count
    }

    /** Učitava top vijesti za zadanu kategoriju */
    fun loadTopStories(bosanskaKategorija: String) {
        val eng = categoryMap[bosanskaKategorija] ?: "all"
        viewModelScope.launch {
            if (eng == "all") {
                val sve = newsDAO.getAllStories()
                _allStoriesFlow.value = sve
                _displayListFlow.value = sve
            } else {
                val trenutno = newsDAO.getAllStories().filter { it.category == eng }
                _displayListFlow.value = trenutno

                try {
                    newsDAO.getTopStoriesByCategory(eng)
                    val updated = newsDAO.getAllStories()
                    _allStoriesFlow.value = updated
                    _displayListFlow.value = updated.filter { it.category == eng }
                    _errorFlow.value = null
                } catch (e: Exception) {
                    // Neuspješan mrežni poziv ne smije srušiti aplikaciju
                    _errorFlow.value = "Greška pri učitavanju vijesti: ${e.message}"
                    Logger.e("Greška pri učitavanju kategorije $eng", e, "NewsViewModel")
                }
            }
        }
    }

    fun filterStories(
        bosanskaKategorija: String,
        startDate: String?,
        endDate: String?,
        unwantedWords: List<String>
    ) {
        viewModelScope.launch {
            var stories = newsDAO.getAllStories()

            // Filtriraj po kategoriji
            val eng = categoryMap[bosanskaKategorija] ?: "all"
            if (eng != "all") {
                stories = stories.filter { it.category == eng }
            }

            // Filtriraj po datumu (ako su odabrani)
            if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()) {
                try {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val start = java.time.LocalDate.parse(startDate, formatter)
                    val end = java.time.LocalDate.parse(endDate, formatter)
                    stories = stories.filter {
                        try {
                            val published = java.time.LocalDate.parse(it.publishedDate, formatter)
                            !published.isBefore(start) && !published.isAfter(end)
                        } catch (e: Exception) { false }
                    }
                } catch (e: Exception) { /* ignore bad format */ }
            }

            // Filtriraj po nepoželjnim riječima
            if (unwantedWords.isNotEmpty()) {
                stories = stories.filter { item ->
                    unwantedWords.none { word ->
                        item.title.contains(word, ignoreCase = true) ||
                                item.snippet.contains(word, ignoreCase = true)
                    }
                }
            }

            _displayListFlow.value = stories
        }
    }

    fun searchNews(searchString: String) {
        viewModelScope.launch {
            try {
                val lista = newsDAO.searchNews(searchString)
                val updated = newsDAO.getAllStories()
                _allStoriesFlow.value = updated
                _displayListFlow.value = lista
                _errorFlow.value = null
            } catch (e: Exception) {
                _displayListFlow.value = emptyList()
                _errorFlow.value = "Greška pri pretrazi: ${e.message}"
                Logger.e("Greška pri pretrazi '$searchString'", e, "NewsViewModel")
            }
        }
    }

    /** Učitava slične vijesti za zadani UUID */
    fun loadSimilarStories(uuid: String) {
        viewModelScope.launch {
            try {
                val lista = newsDAO.getSimilarStories(uuid)
                val updated = newsDAO.getAllStories()
                _allStoriesFlow.value = updated
                _similarStoriesFlow.value = lista
                _errorFlow.value = null
            } catch (e: Exception) {
                _similarStoriesFlow.value = emptyList()
                Logger.e("Greška pri učitavanju sličnih vijesti za $uuid", e, "NewsViewModel")
            }
        }
    }

    /** Resetuje tagove (npr. pri promjeni vijesti) */
    fun clearImageTags() {
        _imageTagsFlow.value = emptyList()
    }

    /** Učitava tagove za tu sliku */
    fun loadImageTags(imageUrl: String) {
        Logger.d("loadImageTags pozvan za URL: $imageUrl", "NewsViewModel")
        viewModelScope.launch {
            try {
                Logger.d("Pozivam imaggaDAO.getTags", "NewsViewModel")
                val tagStrings = imaggaDAO.getTags(imageUrl)
                Logger.d("Dobio ${tagStrings.size} tagova", "NewsViewModel")

                // Uzmi samo top 5 tagova za bolju preglednost
                val topTags = tagStrings.take(5).map { etf.ri.rma.newsfeedapp.data.ImaggaTag(it) }
                _imageTagsFlow.value = topTags
                Logger.d("Ažurirao imageTagsFlow sa ${topTags.size} tagova", "NewsViewModel")

                val all = newsDAO.getAllStories().toMutableList()
                val item = all.find { it.imageUrl == imageUrl }
                item?.let {
                    it.imageTags.clear()
                    it.imageTags.addAll(topTags)
                    Logger.d("Dodao tagove u NewsItem", "NewsViewModel")
                }
            } catch (e: Exception) {
                Logger.e("Greška pri učitavanju tagova", e, "NewsViewModel")
                _imageTagsFlow.value = emptyList()
            }
        }
    }
}