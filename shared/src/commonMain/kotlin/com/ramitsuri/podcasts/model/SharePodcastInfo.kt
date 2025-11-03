package com.ramitsuri.podcasts.model

import kotlinx.serialization.Serializable

@Serializable
data class SharePodcastInfo(
    val podcastName: String,
    val episodeTitle: String,
    val url: String,
) {
    val allValues: String
        get() = "$podcastAndEpisode\n$url"

    val podcastAndEpisode: String
        get() = "$podcastName: $episodeTitle"
}
