package com.ramitsuri.podcasts.network.api.interfaces

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.model.PodcastResponseDto
import com.ramitsuri.podcasts.network.model.PodcastsResponseDto
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import com.ramitsuri.podcasts.network.model.TrendingPodcastsRequest
import com.ramitsuri.podcasts.network.model.TrendingPodcastsResponseDto

internal interface PodcastsApi {
    suspend fun getTrending(request: TrendingPodcastsRequest): PodcastResult<TrendingPodcastsResponseDto>

    suspend fun search(request: SearchPodcastsRequest): PodcastResult<PodcastsResponseDto>

    suspend fun getById(id: Long): PodcastResult<PodcastResponseDto>
}
