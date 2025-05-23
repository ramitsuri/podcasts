package com.ramitsuri.podcasts.network.api

import com.ramitsuri.podcasts.model.PodcastError
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
            httpClient.get(urlString = "$baseUrl/podcasts/trending") {
                url {
                    parameters.apply {
                        append("max", request.maxResults.toString())
                        append("since", request.sinceEpochSeconds.toString())
                        append("lang", request.languages.joinToString(separator = ","))
                        if (request.categories.isNotEmpty()) {
                            append("cat", request.categories.joinToString(separator = ","))
                        }
                    }
                }
            }
        }
    }

    override suspend fun search(request: SearchPodcastsRequest): PodcastResult<PodcastsResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(urlString = "$baseUrl/search/byterm") {
                url {
                    parameters.apply {
                        append("q", request.term)
                        append("max", request.maxResults.toString())
                        append("fulltext", "true")
                        append("similar", request.findSimilar.toString())
                        append("clean", "true")
                    }
                }
            }
        }
    }

    override suspend fun getById(id: Long): PodcastResult<PodcastResponseDto> {
        return apiRequest(ioDispatcher) {
            httpClient.get(urlString = "$baseUrl/podcasts/byfeedid") {
                url {
                    parameters.append("id", id.toString())
                }
            }
        }
    }

    override suspend fun getByUrl(url: String): PodcastResult<PodcastResponseDto> {
        return try {
            apiRequest(ioDispatcher) {
                httpClient.get(urlString = "$baseUrl/podcasts/byfeedurl") {
                    url {
                        encodedParameters.append("url", url)
                    }
                }
            }
        } catch (e: Exception) {
            PodcastResult.Failure(PodcastError.Unknown(null))
        }
    }
}
