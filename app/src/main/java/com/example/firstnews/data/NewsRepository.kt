package com.example.firstnews.data

import androidx.paging.*
import androidx.room.Query
import androidx.room.withTransaction
import com.example.firstnews.api.NewsAPI
import com.example.firstnews.util.Resource
import com.example.firstnews.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOError
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NewsRepository @Inject constructor(
    private val newsAPI: NewsAPI,
    private val articleDatabase: NewsArticleDatabase
) {

    private val newsDAO: NewsArticleDao =
        articleDatabase.newsArticleDao()

    fun getBreakingNews(
        forceRefresh: Boolean,
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
                val bookmarkedArticles = newsDAO.getAllBookmarkedArticles().first()

                val breakingNewsArticles =
                    serverBreakingNewsArticles.map {
                        val isBookmarked = bookmarkedArticles.any {bookmarkedArticle ->
                            bookmarkedArticle.url == it.url
                        }

                        NewsArticle(
                            title = it.title,
                            url = it.url,
                            thumbnailURL = it.urlToImage,
                            isBookmarked = isBookmarked
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
            shouldFetch = { cashedArticles ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedArticles = cashedArticles.sortedBy {
                        it.updatedAt
                    }
                    val oldestTimeStamp = sortedArticles.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimeStamp == null ||
                            oldestTimeStamp < System.currentTimeMillis() -
                            TimeUnit.MINUTES.toMillis(60)
                    needsRefresh
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

    @OptIn(ExperimentalPagingApi::class)
    fun getSearchResultsPaged(query: String, refreshOnInit: Boolean): Flow<PagingData<NewsArticle>> =
    Pager(
        config = PagingConfig(pageSize = 20, maxSize = 200),
        remoteMediator = SearchNewsRemoteMediator(query, newsAPI, articleDatabase, refreshOnInit),
        pagingSourceFactory = { newsDAO.getSearchResultsArticlesPaged(query)}
    ).flow


    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsDAO.getAllBookmarkedArticles()

    suspend fun resetAllBookmarks() {
        newsDAO.resetAllBookmarks()
    }


    suspend fun updateArticle(article: NewsArticle){
        newsDAO.updateArticle(article)
    }

    suspend fun deleteAllArticlesOlderThan(timestampInMillis: Long){
        newsDAO.deleteAllArticlesOlderThan(timestampInMillis)
    }


}