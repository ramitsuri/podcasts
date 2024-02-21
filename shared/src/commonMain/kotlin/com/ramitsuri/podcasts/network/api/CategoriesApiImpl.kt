package com.ramitsuri.podcasts.network.api

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.CategoriesApi
import com.ramitsuri.podcasts.network.model.CategoriesResponseDto
import com.ramitsuri.podcasts.network.utils.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher

internal class CategoriesApiImpl(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : CategoriesApi {
    override suspend fun get(): PodcastResult<CategoriesResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get("$baseUrl/categories/list")
        }
    }
}
