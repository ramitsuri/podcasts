package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState

data class EpisodeDetailsViewState(
    val episode: Episode? = null,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
)
