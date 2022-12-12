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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Refresh> { }
    val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    var pendingScrollToTopLocation = false

    val breakingNews = refreshTrigger.flatMapLatest {

        newsRepository.getBreakingNews(
            it == Refresh.FORCE,
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
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }

    }

    init {
        viewModelScope.launch {
            newsRepository.deleteAllArticlesOlderThan(
                System.currentTimeMillis() -
                        TimeUnit.DAYS.toMillis(7)
            )
        }
    }

    fun onBookmarkedClicked(article: NewsArticle){
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)

        viewModelScope.launch {
            newsRepository.updateArticle(updatedArticle)
        }
    }

    fun onStart() {
        if (breakingNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    enum class Refresh{
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val message: Throwable) : Event()
    }

}

