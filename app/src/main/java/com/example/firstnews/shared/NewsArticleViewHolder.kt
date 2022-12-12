package com.example.firstnews.shared

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.firstnews.R
import com.example.firstnews.databinding.ItemNewsArticleBinding
import com.example.firstnews.data.NewsArticle

class NewsArticleViewHolder(
    private val binding: ItemNewsArticleBinding,
    private val onItemClicked: (Int) -> Unit,
    private val onBookmarkedClicked: (Int) -> Unit
) : ViewHolder(binding.root) {

    fun bind(article: NewsArticle) {
        binding.apply {
            Glide.with(itemView)
                .load(article.thumbnailURL)
                .error(R.drawable.ic_bookmarks)
                .into(imageView)

            textViewTitle.text = article.title ?: ""

            imageViewBookmark.setImageResource(
                when {
                    article.isBookmarked -> R.drawable.ic_bookmark_selected
                    else -> R.drawable.ic_bookmark_unselected
                }
            )
        }
    }

    init {
        binding.apply {
            root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(position)
                }
            }

            imageViewBookmark.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookmarkedClicked(position)
                }
            }
        }
    }
}