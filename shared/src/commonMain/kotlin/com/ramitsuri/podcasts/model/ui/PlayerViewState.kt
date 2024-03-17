package com.ramitsuri.podcasts.model.ui

import kotlin.time.Duration

data class PlayerViewState(
    val isExpanded: Boolean,
    val isPlaying:Boolean,
    val episodeTitle: String,
    val episodeArtworkUrl: String,
    val podcastName: String,
    val sleepTimer: SleepTimer,
    val playbackSpeed: Float,
    val isCasting: Boolean,
    val progress: Float,
    val playedDuration: Duration,
    val remainingDuration: Duration,
)
