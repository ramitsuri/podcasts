package com.ramitsuri.podcasts.network.api

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.EpisodesResponseDto
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.network.utils.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher

internal class EpisodesApiImpl(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
    private val isDebug: Boolean,
) : EpisodesApi {
    override suspend fun getByPodcastId(request: GetEpisodesRequest): PodcastResult<EpisodesResponseDto> {
        if (isDebug) {
            return PodcastResult.Success(EpisodesResponseDto(listOf()))
        }
        return apiRequest(ioDispatcher) {
            httpClient.get(urlString = "$baseUrl/episodes/byfeedid") {
                url {
                    parameters.apply {
                        append("id", request.id.toString())
                        if (request.sinceEpochSeconds != null) {
                            append("since", request.sinceEpochSeconds.toString())
                        }
                        append("max", request.max.toString())
                        append("fulltext", "true")
                    }
                }
            }
        }
    }
}
