package etf.ri.rma.newsfeedapp.model

data class NewsItem(
    // Ovo je stvarni “UUID” kojeg testovi traže
    val uuid: String,

    // Ostala polja nazivamo potpuno isto kao prije
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,
    var isFeatured: Boolean,
    val source: String,
    val publishedDate: String
) {

    val id: String
        get() = uuid
}
