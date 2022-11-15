package com.example.firstnews.features.breakingnews

import android.media.metrics.Event
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstnews.data.NewsArticle
import com.example.firstnews.data.NewsRepository
import com.example.firstnews.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Unit> { }
    val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    var pendingScrollToTopLocation = false

    val breakingNews = refreshTrigger.flatMapLatest {
        newsRepository.getBreakingNews(
            onFetchSuccess = {
                pendingScrollToTopLocation = true
            },
            onFetchFailed = { t ->
                viewModelScope.launch {
                    eventChannel.send(Event.ShowErrorMessage(t))
                }
            }
        )
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onManualRefresh() {
        if (breakingNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Unit)
            }
        }

    }

    fun onStart() {
        if (breakingNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Unit)
            }
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val message: Throwable) : Event()
    }

}

