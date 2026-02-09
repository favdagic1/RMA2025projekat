package com.favdagic.newsfeedapp.model

data class NewsItem(

    val uuid: String,
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,

    var isFeatured: Boolean,
    val imageTags: ArrayList<com.favdagic.newsfeedapp.data.ImaggaTag> = arrayListOf(),
    val source: String,
    val publishedDate: String


)
