package com.example.firstnews.data

import androidx.room.withTransaction
import com.example.firstnews.api.NewsAPI
import com.example.firstnews.util.Resource
import com.example.firstnews.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOError
import java.io.IOException
import javax.inject.Inject


class NewsRepository @Inject constructor(
    private val newsAPI: NewsAPI,
    private val articleDatabase: NewsArticleDatabase
) {

    private val newsDAO: NewsArticleDao =
        articleDatabase.newsArticleDao()

    fun getBreakingNews(
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsDAO.getAllBreakingNewsArticles()
            },
            fetch = {
                val response = newsAPI.getBreakingNews()
                response.articles
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                val breakingNewsArticles =
                    serverBreakingNewsArticles.map {
                        NewsArticle(
                            title = it.title,
                            url = it.url,
                            thumbnailURL = it.urlToImage,
                            isBookmarked = false
                        )
                    }

                val breakingNews = breakingNewsArticles.map {
                    BreakingNews(
                        articleUrl = it.url
                    )
                }

                articleDatabase.withTransaction {
                    newsDAO.deleteBreakingNews()
                    newsDAO.insertBreakingNews(breakingNews)
                    newsDAO.insertArticles(breakingNewsArticles)
                }
            },

            onFetchSuccess = onFetchSuccess,

            onFetchFailed = {
                if (it !is HttpException && it !is IOException) {
                    throw it
                }
                onFetchFailed(it)
            }


        )


}