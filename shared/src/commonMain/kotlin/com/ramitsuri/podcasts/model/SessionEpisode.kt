package com.ramitsuri.podcasts.model

import kotlin.time.Instant

data class SessionEpisode(
    val episodeId: String,
    val sessionId: String,
    val time: Instant,
)
