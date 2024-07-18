package com.ramitsuri.podcasts.player

import com.ramitsuri.podcasts.model.Episode
import kotlin.time.Duration

interface PlayerController {
    fun initializePlayer()

    fun releasePlayer()

    fun play(episode: Episode, queue: List<Episode> = listOf())

    fun pause()

    fun seek(to: Duration)

    fun addToQueue(episode: Episode)

    fun removeFromQueue(episode: Episode)

    fun swapInQueue(episode1: Episode, episode2: Episode)
}
