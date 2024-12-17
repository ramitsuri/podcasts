package com.ramitsuri.podcasts.model

import kotlin.time.Duration

data class Session(
    val sessionId: String,
    val episodeId: String,
    val podcastId: Long,
    val duration: Duration,
    val playbackSpeed: Float,
)
