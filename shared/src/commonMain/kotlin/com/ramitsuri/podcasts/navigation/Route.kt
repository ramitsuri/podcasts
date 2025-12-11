package com.ramitsuri.podcasts.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    object Home : Route

    @Serializable
    object ImportSubscriptions : Route

    @Serializable
    object Explore : Route

    @Serializable
    object Library : Route

    @Serializable
    data class EpisodeDetails(
        @SerialName("episode_id")
        val episodeId: String,
        @SerialName("podcast_id")
        val podcastId: Long,
    ) : Route

    @Serializable
    object Queue : Route

    @Serializable
    data class PodcastDetails(
        @SerialName("podcast_id")
        val podcastId: Long,
        @SerialName("refresh_podcast")
        val refreshPodcast: Boolean,
    ) : Route

    @Serializable
    object Subscriptions : Route

    @Serializable
    object Downloads : Route

    @Serializable
    object Favorites : Route

    @Serializable
    object EpisodeHistory : Route

    @Serializable
    object Settings : Route

    @Serializable
    object YearEndReview : Route

    @Serializable
    object BackupRestore : Route

    @Serializable
    object Search : Route
}
