package com.ramitsuri.podcasts.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EpisodesResponseDto(
    @SerialName("items")
    val items: List<EpisodeDto>
)
