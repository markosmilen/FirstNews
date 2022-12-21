package com.example.firstnews.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface SearchQueryRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(remoteKey: SearchQueryRemoteKey)


    @Query("SELECT * FROM search_query_remote_key WHERE searchQuery = :searchQuery")
    suspend fun getRemoteKey(searchQuery: String): SearchQueryRemoteKey

}