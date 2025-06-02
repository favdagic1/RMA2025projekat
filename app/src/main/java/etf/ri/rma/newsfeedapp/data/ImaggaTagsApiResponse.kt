package etf.ri.rma.newsfeedapp.data

import com.google.gson.annotations.SerializedName

data class ImaggaTagsApiResponse(
    @SerializedName("result")
    val result: TagsResult
)

data class TagsResult(
    @SerializedName("tags")
    val tags: List<Tag>
)

data class Tag(
    @SerializedName("confidence")
    val confidence: Float,
    @SerializedName("tag")
    val tag: TagValue
)

data class TagValue(
    @SerializedName("en")
    val en: String
)
