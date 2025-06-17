package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.data.NewsApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v1/news/top")
    suspend fun getTopStoriesByCategory(
        @Query("categories") categories: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse

    @GET("v1/news/similar")
    suspend fun getSimilarStories(
        @Query("uuid") uuid: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse


    @GET("v1/news/all")
    suspend fun searchNews(
        @Query("search") query: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse
}
