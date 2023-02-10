package com.example.firstnews.features.searchnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.room.Query
import com.example.firstnews.data.NewsArticle
import com.example.firstnews.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val currentQuery = MutableStateFlow<String?>(null)

    val hasCurrentQuery = currentQuery.map { it != null }

    val searchResults = currentQuery.flatMapLatest { query ->
        query?.let {
            repository.getSearchResultsPaged(query)
        } ?: emptyFlow()
    }.cachedIn(viewModelScope)

    var refreshInProgress = false
    var pendingScrollToTopAfterRefresh = false
    var pendingScrollToTopAfterNewQuery = false
    var newQueryInProgress = false

    fun onSearchQuerySubmit(query: String) {
        currentQuery.value = query
        pendingScrollToTopAfterNewQuery = true
        newQueryInProgress = true
    }

    fun onBookmarkedClicked(article: NewsArticle){
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)

        viewModelScope.launch {
            repository.updateArticle(updatedArticle)
        }
    }
}