package com.favdagic.newsfeedapp.data.network.api

import com.favdagic.newsfeedapp.data.ImaggaTagsApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ImagaApiService {
    @GET("tags")
    suspend fun getTags(
        @Query("image_url") imageUrl: String,
        @Header("Authorization") authorization: String      // npr. "Basic base64(apiKey:apiSecret)"
    ): ImaggaTagsApiResponse

}
