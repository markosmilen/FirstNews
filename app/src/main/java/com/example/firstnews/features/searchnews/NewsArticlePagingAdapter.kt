package com.example.firstnews.features.searchnews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ListAdapter
import com.example.firstnews.databinding.ItemNewsArticleBinding
import com.example.firstnews.data.NewsArticle
import com.example.firstnews.shared.NewsArticleComparator
import com.example.firstnews.shared.NewsArticleViewHolder

class NewsArticlePagingAdapter(
    private val onItemClicked: (NewsArticle) -> Unit,
    private val onBookmarkedClicked: (NewsArticle) -> Unit
) :
    PagingDataAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val binding =
            ItemNewsArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsArticleViewHolder(binding,
            onItemClicked = { position ->
                val article = getItem(position)
                if (article != null){
                    onItemClicked(article)
                }
            },
            onBookmarkedClicked = { position ->
                val article = getItem(position)
                if (article != null){
                    onBookmarkedClicked(article)
                }
            })
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
}