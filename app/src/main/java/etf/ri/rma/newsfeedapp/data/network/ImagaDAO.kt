package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.ImaggaTagsApiResponse
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.MalformedURLException
import java.net.URL

class ImagaDAO {
    private lateinit var apiService: ImagaApiService
    private val tagsCache = mutableMapOf<String, List<String>>()
    private val IMAGGA_API_TOKEN = "acc_bd7c68cd93e2991"

    fun setApiService(service: ImagaApiService) {
        apiService = service
    }

    suspend fun getTags(imageUrl: String): List<String> {
        try {
            URL(imageUrl)
        } catch (e: MalformedURLException) {
            throw InvalidImageURLException("Neispravan URL: $imageUrl")
        }

        tagsCache[imageUrl]?.let { return it }

        val response = apiService.getTags(imageUrl, IMAGGA_API_TOKEN)
        val fetchedTags = response.result.tags.map { it.tag.en }
        tagsCache[imageUrl] = fetchedTags
        return fetchedTags
    }

    companion object {

        fun createWithBaseUrl(baseUrl: String): ImagaDAO {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(ImagaApiService::class.java)
            val dao = ImagaDAO()
            dao.setApiService(service)
            return dao
        }
    }
}
