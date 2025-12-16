package com.ramitsuri.podcasts.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute

@Serializable
sealed interface Route : NavKey {
    @Serializable
    object Home : Route, TopLevelRoute

    @Serializable
    object ImportSubscriptions : Route

    @Serializable
    object Explore : Route, TopLevelRoute

    @Serializable
    object Library : Route, TopLevelRoute

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
