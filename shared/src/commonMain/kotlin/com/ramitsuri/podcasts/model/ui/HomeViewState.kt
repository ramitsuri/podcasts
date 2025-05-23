package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast

data class HomeViewState(
    val subscribedPodcasts: List<Podcast> = listOf(),
    val episodes: List<Episode> = listOf(),
    val currentlyPlayingEpisodeId: String? = null,
    val currentlyPlayingEpisodeState: PlayingState = PlayingState.NOT_PLAYING,
    val currentlyPlayingEpisodeArtworkUrl: String? = null,
    val showYearEndReview: Boolean = false,
    val isRefreshing: Boolean = false,
    val showSettingsBadge: Boolean = false,
)
