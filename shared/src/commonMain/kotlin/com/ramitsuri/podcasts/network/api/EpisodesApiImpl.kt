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
) : EpisodesApi {
    override suspend fun getByPodcastId(request: GetEpisodesRequest): PodcastResult<EpisodesResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(
                urlString =
                    "$baseUrl/episodes/byfeedid" +
                        "?id=${request.id}" +
                        "&since=${request.sinceTime.epochSeconds}" +
                        "&max=$MAX_COUNT" +
                        "&fulltext=$GET_FULL_DESCRIPTION",
            )
        }
    }

    companion object {
        private const val MAX_COUNT = 10
        private const val GET_FULL_DESCRIPTION = true
    }
}
