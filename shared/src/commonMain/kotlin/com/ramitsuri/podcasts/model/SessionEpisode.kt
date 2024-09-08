package com.ramitsuri.podcasts.model

import kotlinx.datetime.Instant

data class SessionEpisode(
    val episodeId: String,
    val sessionId: String,
    val time: Instant,
)
