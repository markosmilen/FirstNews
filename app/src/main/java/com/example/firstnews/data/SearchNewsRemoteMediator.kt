package com.example.firstnews.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.firstnews.api.NewsAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

private const val NEWS_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsApi: NewsAPI,
    private val newsArticleDb: NewsArticleDatabase
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsArticleDb.newsArticleDao()
    private val searchQueryRemoteKeyDao = newsArticleDb.searchQueryRemoteKeyDAO()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType){
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = newsApi.searchNews(searchQuery, page, state.config.pageSize)
            delay(3000)
            val serverSearchArticles = response.articles

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()

            val searchResultArticles = serverSearchArticles.map { serverSearchResultArticle ->
                val isbookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                    bookmarkedArticle.url == serverSearchResultArticle.url
                }

                NewsArticle(
                    title = serverSearchResultArticle.title,
                    url = serverSearchResultArticle.url,
                    thumbnailURL = serverSearchResultArticle.urlToImage,
                    isBookmarked = isbookmarked
                )
            }

            newsArticleDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.deleteSearchResultsForQuery(searchQuery)
                }
                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery, article.url, queryPosition++)
                }

                val nextPageKey = page + 1

                newsArticleDao.insertArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
                searchQueryRemoteKeyDao.insertRemoteKeys(
                    SearchQueryRemoteKey(
                        searchQuery,
                        nextPageKey
                    )
                )

            }
            return MediatorResult.Success(endOfPaginationReached = serverSearchArticles.isEmpty())
        } catch (exception: IOException) {
            return MediatorResult.Error (exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error (exception)
        }
    }

}