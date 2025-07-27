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
        println("DEBUG IMAGGA: getTags pozvan za URL: $imageUrl")

        try {
            URL(imageUrl)
        } catch (e: MalformedURLException) {
            println("DEBUG IMAGGA: Neispravan URL: $imageUrl")
            throw InvalidImageURLException("Neispravan URL: $imageUrl")
        }

        tagsCache[imageUrl]?.let {
            println("DEBUG IMAGGA: Vraćam cache-ovane tagove za $imageUrl")
            return it
        }

        try {
            println("DEBUG IMAGGA: Pozivam Imagga API za $imageUrl")
            val response = apiService.getTags(imageUrl, IMAGGA_API_TOKEN)
            println("DEBUG IMAGGA: API odgovorio, broj tagova: ${response.result.tags.size}")

            val fetchedTags = response.result.tags.map { it.tag.en }
            println("DEBUG IMAGGA: Tagovi: ${fetchedTags.joinToString(", ")}")

            tagsCache[imageUrl] = fetchedTags
            return fetchedTags
        } catch (e: Exception) {
            println("DEBUG IMAGGA: Greška pri pozivanju API-ja: ${e.message}")
            println("DEBUG IMAGGA: Stack trace: ${e.printStackTrace()}")
            return emptyList()
        }
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