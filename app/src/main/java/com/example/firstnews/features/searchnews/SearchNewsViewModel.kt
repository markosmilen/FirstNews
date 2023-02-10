package com.example.firstnews.features.searchnews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
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
    private val repository: NewsRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val currentQuery = state.getLiveData<String?>("currentQuery", null)
        //MutableStateFlow<String?>(null)

    val hasCurrentQuery = currentQuery.asFlow().map { it != null }

    private var refreshOnInit = false

    val searchResults = currentQuery.asFlow().flatMapLatest { query ->
        query?.let {
            repository.getSearchResultsPaged(query, refreshOnInit)
        } ?: emptyFlow()
    }.cachedIn(viewModelScope)

    var refreshInProgress = false
    var pendingScrollToTopAfterRefresh = false
    var pendingScrollToTopAfterNewQuery = false
    var newQueryInProgress = false

    fun onSearchQuerySubmit(query: String) {
        refreshOnInit = true
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