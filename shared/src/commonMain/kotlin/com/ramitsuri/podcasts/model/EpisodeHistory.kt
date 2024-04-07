package com.ramitsuri.podcasts.model

import kotlinx.datetime.Instant

data class EpisodeHistory(
    val episode: Episode,
    val time: Instant,
)
