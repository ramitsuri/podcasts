package com.ramitsuri.podcasts.network.utils

import com.ramitsuri.podcasts.model.PodcastError
import com.ramitsuri.podcasts.model.PodcastResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal suspend inline fun <reified T> apiRequest(
    ioDispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> HttpResponse,
): PodcastResult<T> {
    return withContext(ioDispatcher) {
        var exception: Throwable? = null
        val response: HttpResponse? =
            try {
                call()
            } catch (e: Exception) {
                exception = e
                null
            }
        return@withContext when {
            response?.status == HttpStatusCode.OK -> {
                val data: T = response.body()
                PodcastResult.Success(data)
            }

            response?.status == HttpStatusCode.Created && T::class == Unit::class -> {
                PodcastResult.Success(response.body())
            }

            exception is IOException -> {
                PodcastResult.Failure(PodcastError.NoInternet(exception))
            }

            else -> {
                PodcastResult.Failure(PodcastError.Unknown(exception))
            }
        }
    }
}
