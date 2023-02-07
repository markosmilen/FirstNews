package com.example.firstnews.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM breaking_news INNER JOIN news_articles ON articleUrl = url")
    fun getAllBreakingNewsArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM SEARCH_RESULT INNER JOIN news_articles ON articleUrl == url WHERE  searchQuery = :query ORDER BY queryPosition")
    fun getSearchResultsArticlesPaged(query: String): PagingSource<Int, NewsArticle>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakingNews(breakingNews: List<BreakingNews>)

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("UPDATE news_articles SET isBookmarked = 0")
    suspend fun resetAllBookmarks()

    @Query("DELETE FROM breaking_news")
    suspend fun deleteBreakingNews()

    @Query("DELETE FROM news_articles WHERE updatedAt < :timeStampInMillis AND isBookmarked = 0")
    suspend fun deleteAllArticlesOlderThan(timeStampInMillis: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Query("DELETE FROM search_result WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("SELECT max(searchQuery) FROM search_result WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String): Int?
}