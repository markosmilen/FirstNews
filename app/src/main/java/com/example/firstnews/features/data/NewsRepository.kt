package com.example.firstnews.features.data

import com.example.firstnews.features.api.NewsAPI
import javax.inject.Inject


class NewsRepository @Inject constructor(
    val newsAPI: NewsAPI,
    val articleDatabase: NewsArticleDatabase
        ) {

    private val newsDAO: NewsArticleDao =
        articleDatabase.newsArticleDao()
}