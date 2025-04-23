package com.ramitsuri.podcasts.player

import com.ramitsuri.podcasts.model.Episode

interface PlayerController {
    fun initializePlayer()

    fun releasePlayer()

    fun play(episode: Episode)

    fun playCurrentEpisode()

    fun pause()

    suspend fun seek(byPercentOfDuration: Float)

    suspend fun skip()

    suspend fun replay()

    fun updateQueue()

    fun logQueue(): List<String>

    fun playNext()

    fun hasNext(): Boolean
}
