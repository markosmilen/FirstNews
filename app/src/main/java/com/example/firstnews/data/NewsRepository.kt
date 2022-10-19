package com.example.firstnews.data

import com.example.firstnews.api.NewsAPI
import javax.inject.Inject


class NewsRepository @Inject constructor(
    private val newsAPI: NewsAPI,
    private val articleDatabase: NewsArticleDatabase
        ) {

    private val newsDAO: NewsArticleDao =
        articleDatabase.newsArticleDao()

    suspend fun getBreakingNews(): List<NewsArticle> {
        val response = newsAPI.getBreakingNews()
        val serverNewsArticles = response.articles
        val breakingNews = serverNewsArticles.map { serverNewsArticle ->
            NewsArticle(
                title = serverNewsArticle.title,
                url = serverNewsArticle.url,
                thumbnailURL = serverNewsArticle.urlToImage,
                isBookmarked = false
            )
        }
        return breakingNews
    }
}