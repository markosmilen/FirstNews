package com.example.firstnews.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Insert
import com.example.firstnews.data.NewsArticle
import com.example.firstnews.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    val bookmarks = newsRepository.getAllBookmarkedArticles()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)


    fun onBookmarkedClicked(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)

        viewModelScope.launch {
            newsRepository.updateArticle(updatedArticle)
        }
    }

    fun onDeleteAllBookmarks() {
        viewModelScope.launch {
            newsRepository.resetAllBookmarks()
        }
    }
}