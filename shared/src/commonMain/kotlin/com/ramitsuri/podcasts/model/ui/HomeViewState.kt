package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode

data class HomeViewState(
    val episodes: List<Episode> = listOf(),
    val currentlyPlayingEpisodeId: String? = null,
)
