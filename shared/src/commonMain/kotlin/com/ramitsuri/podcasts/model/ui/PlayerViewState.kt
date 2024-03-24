package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.PlayingState
import kotlin.time.Duration

data class PlayerViewState(
    val hasEverBeenPlayed: Boolean = false,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
    val episodeTitle: String = "",
    val episodeArtworkUrl: String = "",
    val podcastName: String = "",
    val sleepTimer: SleepTimer = SleepTimer.None,
    val sleepTimerDuration: Duration? = null,
    val playbackSpeed: Float = 1f,
    val isCasting: Boolean = false,
    val progress: Float = 0f,
    val playedDuration: Duration = Duration.ZERO,
    val remainingDuration: Duration? = Duration.ZERO,
    val totalDuration: Duration? = Duration.ZERO,
    val trimSilence: Boolean = false,
)
