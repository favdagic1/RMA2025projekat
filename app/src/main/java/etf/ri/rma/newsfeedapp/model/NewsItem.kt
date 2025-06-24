package etf.ri.rma.newsfeedapp.model

data class NewsItem(

    val uuid: String,
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,

    var isFeatured: Boolean,
    val imageTags: ArrayList<etf.ri.rma.newsfeedapp.data.ImaggaTag> = arrayListOf(),
    val source: String,
    val publishedDate: String


)
