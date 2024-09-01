package com.ramitsuri.podcasts.model

import kotlinx.datetime.Instant

data class EpisodeHistory(
    val episode: Episode,
    val sessionId: String,
    val time: Instant,
)
