package etf.ri.rma.newsfeedapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsViewModel : ViewModel() {

    // 1) Retrofit i DAO-e
    private val newsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.thenewsapi.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val imaggaRetrofit = Retrofit.Builder()
        .baseUrl("https://api.imagga.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val newsApiService: NewsApiService = newsRetrofit.create(NewsApiService::class.java)
    private val imaggaApiService: ImagaApiService = imaggaRetrofit.create(ImagaApiService::class.java)


    private val newsDAO = NewsDAO().apply { setApiService(newsApiService) }
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

    // 6) Mapiranje bosanski → engleski ključevi
    private val categoryMap = mapOf(
        "Sve" to "all",
        "Politika" to "politics",
        "Sport" to "sports",
        "Nauka/tehnologija" to "science"
    )

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
                    _allStoriesFlow.value = newsDAO.getAllStories()
                    _displayListFlow.value = newsDAO.getAllStories().filter { it.category == eng }
                } catch (e: Exception) {
                    // Neuspješan mrežni poziv ne smije srušiti aplikaciju
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
                _allStoriesFlow.value = newsDAO.getAllStories()
                _displayListFlow.value = lista
            } catch (e: Exception) {
                _displayListFlow.value = emptyList()
            }
        }
    }





    /** Učitava slične vijesti za zadani UUID */
    fun loadSimilarStories(uuid: String) {
        viewModelScope.launch {
            try {
                val lista = newsDAO.getSimilarStories(uuid)
                _similarStoriesFlow.value = lista
            } catch (e: Exception) {
                _similarStoriesFlow.value = emptyList()
            }
        }
    }

    /** Učitava tagove za tu sliku */
    fun loadImageTags(imageUrl: String) {
        viewModelScope.launch {
            try {
                val tagStrings = imaggaDAO.getTags(imageUrl)
                val tags = tagStrings.map { etf.ri.rma.newsfeedapp.data.ImaggaTag(it) }
                _imageTagsFlow.value = tags
                val all = newsDAO.getAllStories().toMutableList()
                val item = all.find { it.imageUrl == imageUrl }
                item?.let {
                    it.imageTags.clear()
                    it.imageTags.addAll(tags)
                }
            } catch (e: Exception) {
                _imageTagsFlow.value = emptyList()
            }
        }
    }
}
