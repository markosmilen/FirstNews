package com.example.firstnews.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_query_remote_key")
data class SearchQueryRemoteKey(
    @PrimaryKey val searchQuery: String,
    val nextPageKey: Int
)