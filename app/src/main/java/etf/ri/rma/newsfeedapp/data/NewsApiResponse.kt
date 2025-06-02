package etf.ri.rma.newsfeedapp.data

import com.google.gson.annotations.SerializedName

data class NewsApiResponse(
    @SerializedName("data")
    val data: List<NewsArticleDto>
)

data class NewsArticleDto(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("snippet") val snippet: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("published_at") val publishedAt: String?
)
