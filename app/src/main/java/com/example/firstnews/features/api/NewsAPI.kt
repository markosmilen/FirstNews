package com.example.firstnews.features.api

import com.example.firstnews.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface NewsAPI {

    companion object {
        const val BASE_URL = "https://newsapi.org"
        const val API_KEY = BuildConfig.NEWS_API_ACCESS_KEY
    }

    @Headers("X-Api-Key= $API_KEY")
    @GET("top-headlines?country=us&pageSize=100")
    suspend fun getBreakingNews(): NewsResponse

    @Headers("X-Api-Key= $API_KEY")
    @GET("everything?")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponse


}