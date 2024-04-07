package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import kotlinx.datetime.LocalDate

data class EpisodeHistoryViewState(
    val episodesByDate: Map<LocalDate, List<Episode>> = mapOf(),
    val currentlyPlayingEpisodeId: String? = null,
    val currentlyPlayingEpisodeState: PlayingState = PlayingState.NOT_PLAYING,
)
