package com.ramitsuri.podcasts.model.ui

import kotlin.time.Instant

sealed interface SleepTimer {
    data object None : SleepTimer

    data object EndOfEpisode : SleepTimer

    data class Custom(val time: Instant) : SleepTimer
}
