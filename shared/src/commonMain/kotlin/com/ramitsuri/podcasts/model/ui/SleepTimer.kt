package com.ramitsuri.podcasts.model.ui

import kotlin.time.Duration

sealed interface SleepTimer {
    data object None : SleepTimer

    data class EndOfEpisode(val duration: Duration) : SleepTimer

    data class Custom(val duration: Duration) : SleepTimer

    val timerDuration: Duration
        get() =
            when (this) {
                is Custom -> duration
                is EndOfEpisode -> duration
                is None -> Duration.ZERO
            }
}
