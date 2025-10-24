package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.ImaggaTagsApiResponse
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.util.Logger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.MalformedURLException
import java.net.URL
import android.util.Base64

class ImagaDAO {
    private lateinit var apiService: ImagaApiService
    private val tagsCache = mutableMapOf<String, List<String>>()

    private val IMAGGA_API_KEY = etf.ri.rma.newsfeedapp.BuildConfig.IMAGGA_API_KEY
    private val IMAGGA_API_SECRET = etf.ri.rma.newsfeedapp.BuildConfig.IMAGGA_API_SECRET

    fun setApiService(service: ImagaApiService) {
        apiService = service
    }

    suspend fun getTags(imageUrl: String): List<String> {
        Logger.d("getTags pozvan za URL: $imageUrl", "ImagaDAO")

        try {
            URL(imageUrl)
        } catch (e: MalformedURLException) {
            Logger.e("Neispravan URL: $imageUrl", e, "ImagaDAO")
            throw InvalidImageURLException("Neispravan URL: $imageUrl")
        }

        tagsCache[imageUrl]?.let {
            Logger.d("Vraćam cache-ovane tagove za $imageUrl", "ImagaDAO")
            return it
        }

        try {
            Logger.d("Pozivam Imagga API za $imageUrl", "ImagaDAO")

            // Kreiranje Basic Auth header-a
            val credentials = "$IMAGGA_API_KEY:$IMAGGA_API_SECRET"
            val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

            val response = apiService.getTags(imageUrl, basicAuth)
            Logger.d("API odgovorio, broj tagova: ${response.result.tags.size}", "ImagaDAO")

            val fetchedTags = response.result.tags.map { it.tag.en }
            Logger.d("Tagovi: ${fetchedTags.joinToString(", ")}", "ImagaDAO")

            tagsCache[imageUrl] = fetchedTags
            return fetchedTags
        } catch (e: Exception) {
            Logger.e("Greška pri pozivanju API-ja", e, "ImagaDAO")
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