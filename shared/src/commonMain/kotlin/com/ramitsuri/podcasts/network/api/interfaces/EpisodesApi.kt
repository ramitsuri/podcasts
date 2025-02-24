package com.ramitsuri.podcasts.network.api.interfaces

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.model.EpisodeResponseDto
import com.ramitsuri.podcasts.network.model.EpisodesResponseDto
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest

internal interface EpisodesApi {
    suspend fun getByPodcastId(request: GetEpisodesRequest): PodcastResult<EpisodesResponseDto>

    suspend fun getById(
        id: String,
        podcastId: Long,
    ): PodcastResult<EpisodeResponseDto>
}
