package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode

data class EpisodeDetailsViewState(
    val episode: Episode? = null,
    val isPlaying: Boolean = false,
)
