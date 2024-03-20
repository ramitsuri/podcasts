package com.ramitsuri.podcasts.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EpisodeDto(
    @SerialName("guid")
    val id: String,
    @SerialName("feedId")
    val podcastId: Long,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("link")
    val link: String,
    @SerialName("enclosureUrl")
    val enclosureUrl: String,
    @SerialName("datePublished")
    val datePublished: Long,
    @SerialName("duration")
    val duration: Int?,
    @SerialName("explicit")
    val explicit: Int,
    @SerialName("episode")
    val episode: Int?,
    @SerialName("season")
    val season: Int?,
)
