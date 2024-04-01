package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState

data class DownloadsViewState(
    val episodes: List<Episode> = listOf(),
    val currentlyPlayingEpisodeId: String? = null,
    val currentlyPlayingEpisodeState: PlayingState = PlayingState.NOT_PLAYING,
)
