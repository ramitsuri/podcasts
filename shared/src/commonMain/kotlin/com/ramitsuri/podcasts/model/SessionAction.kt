package com.ramitsuri.podcasts.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class SessionAction(
    val id: Long = 0,
    val sessionId: String,
    val podcastId: Long,
    val episodeId: String,
    val time: Instant,
    val action: Action,
    val playbackSpeed: Float,
) {
    constructor(
        episode: Episode,
        sessionId: String,
        action: Action,
        time: Instant = Clock.System.now(),
        playbackSpeed: Float
    ) : this(
        sessionId = sessionId,
        podcastId = episode.podcastId,
        episodeId = episode.id,
        time = time,
        action = action,
        playbackSpeed = playbackSpeed,
    )
}
