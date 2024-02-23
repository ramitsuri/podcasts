package com.ramitsuri.podcasts.network.api

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.PodcastResponseDto
import com.ramitsuri.podcasts.network.model.PodcastsResponseDto
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import com.ramitsuri.podcasts.network.model.TrendingPodcastsRequest
import com.ramitsuri.podcasts.network.model.TrendingPodcastsResponseDto
import com.ramitsuri.podcasts.network.utils.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher

internal class PodcastsApiImpl(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : PodcastsApi {
    override suspend fun getTrending(request: TrendingPodcastsRequest): PodcastResult<TrendingPodcastsResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(
                urlString =
                    "$baseUrl/podcasts/trending" +
                        "?max=$MAX_COUNT" +
                        "&since=${request.since.epochSeconds}",
            )
        }
    }

    override suspend fun search(request: SearchPodcastsRequest): PodcastResult<PodcastsResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(
                urlString =
                    "$baseUrl/search/byterm" +
                        "?q=${request.term}" +
                        "&max=$MAX_COUNT" +
                        "&fulltext=$GET_FULL_DESCRIPTION" +
                        "&similar=$SIMILAR" +
                        "&clean=$NON_EXPLICIT",
            )
        }
    }

    override suspend fun getById(id: Long): PodcastResult<PodcastResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(
                urlString =
                    "$baseUrl/podcasts/byfeedid" +
                        "?id=$id",
            )
        }
    }

    companion object {
        private const val MAX_COUNT = 50
        private const val GET_FULL_DESCRIPTION = true
        private const val NON_EXPLICIT = true
        private const val SIMILAR = true
    }
}
