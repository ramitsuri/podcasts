package com.ramitsuri.podcasts.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PodcastsResponseDto(
    @SerialName("feeds")
    val podcasts: List<PodcastDto>,
)
