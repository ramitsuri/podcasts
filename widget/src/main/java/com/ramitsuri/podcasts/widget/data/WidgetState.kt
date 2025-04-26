package com.ramitsuri.podcasts.widget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface WidgetState {
    @Serializable
    data class CurrentlyPlaying(
        @SerialName("episode_title")
        val episodeTitle: String,
        @SerialName("is_playing")
        val isPlaying: Boolean,
        @SerialName("album_art_uri")
        val albumArtUri: String,
        @SerialName("deep_link_url")
        val deepLinkUrl: String,
    ) : WidgetState

    @Serializable
    data object NeverPlayed : WidgetState
}
