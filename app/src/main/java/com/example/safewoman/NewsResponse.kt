package com.example.safewoman
data class NewsResponse(
    val articles: List<Article>
)

data class Article(
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?
)
