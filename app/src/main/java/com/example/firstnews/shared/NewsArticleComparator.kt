package com.example.firstnews.shared

import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.example.firstnews.data.NewsArticle

class NewsArticleComparator: ItemCallback<NewsArticle>() {

    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem == newItem
}