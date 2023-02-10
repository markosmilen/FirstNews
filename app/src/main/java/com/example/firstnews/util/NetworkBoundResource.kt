package com.example.firstnews.util


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

inline fun <ResultType, RequestType> networkBoundResource (
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onFetchSuccess: () -> Unit = {},
    crossinline onFetchFailed: (Throwable) -> Unit = {}
) = channelFlow {
    val data = query().first()


    if (shouldFetch(data)) {
        val loading = launch {
            query().collect {  send(Resource.Loading(it))}
        }

        try {
            saveFetchResult(fetch())
            onFetchSuccess()
            loading.cancel()
            query().collect {  send(Resource.Success(it))}
        } catch (t: Throwable) {
            loading.cancel()
            onFetchFailed(t)
            query().collect {  send(Resource.Error(it, t))}
        }
    } else {
        query().collect {  send(Resource.Success(it))}
    }
}