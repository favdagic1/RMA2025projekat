package com.favdagic.newsfeedapp.data.network.api

import com.favdagic.newsfeedapp.data.NewsApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v1/news/top")
    suspend fun getTopStoriesByCategory(
        @Query("categories") categories: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse

    @GET("v1/news/similar/{uuid}")
    suspend fun getSimilarStories(
        @retrofit2.http.Path("uuid") uuid: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse


    @GET("v1/news/all")
    suspend fun searchNews(
        @Query("search") query: String,
        @Query("api_token") apiToken: String
    ): NewsApiResponse
}
