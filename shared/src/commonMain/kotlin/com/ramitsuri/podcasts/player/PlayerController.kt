package com.ramitsuri.podcasts.player

import com.ramitsuri.podcasts.model.Episode
import kotlin.time.Duration

interface PlayerController {
    fun initializePlayer()

    fun releasePlayer()

    fun play(episode: Episode)

    fun pause()

    fun seek(to: Duration)
}
