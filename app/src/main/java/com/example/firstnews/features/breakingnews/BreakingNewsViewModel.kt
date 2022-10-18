package com.example.firstnews.features.breakingnews

import androidx.lifecycle.ViewModel
import com.example.firstnews.features.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {
}