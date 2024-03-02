package com.ramitsuri.podcasts.network.api

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.EpisodesResponseDto
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.network.utils.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineDispatcher

internal class EpisodesApiImpl(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : EpisodesApi {
    override suspend fun getByPodcastId(request: GetEpisodesRequest): PodcastResult<EpisodesResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(urlString = "$baseUrl/episodes/byfeedid") {
                url {
                    parameters {
                        append("id", request.id.toString())
                        append("since", request.sinceEpochSeconds.toString())
                        append("max", MAX_COUNT.toString())
                        append("fulltext", "true")
                    }
                }
            }
        }
    }

    companion object {
        private const val MAX_COUNT = 10
    }
}
