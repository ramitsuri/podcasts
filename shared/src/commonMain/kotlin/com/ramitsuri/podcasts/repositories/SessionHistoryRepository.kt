package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionHistoryRepository internal constructor(
    private val sessionActionDao: SessionActionDao,
) {
    private val mutex = Mutex()
    private var previousEpisode: Episode? = null
    private var previousSpeed: Float? = null
    private var previousIsPlaying: Boolean? = null

    suspend fun episodeStart(
        episode: Episode,
        sessionId: String,
        speed: Float,
    ) {
        mutex.withLock {
            if (previousEpisode?.id == episode.id &&
                previousIsPlaying == true
            ) {
                return
            }
            if (previousEpisode?.id == episode.id) {
                log("STARTING ${episode.title}")
                insert(
                    SessionAction(
                        episode = episode,
                        sessionId = sessionId,
                        action = Action.START,
                        playbackSpeed = speed,
                    ),
                )
            } else {
                val prevEpisode = previousEpisode
                if (prevEpisode != null) {
                    log("STOPPING ${previousEpisode?.title}")
                    insert(
                        SessionAction(
                            episode = prevEpisode,
                            sessionId = sessionId,
                            action = Action.STOP,
                            playbackSpeed = speed,
                        ),
                    )
                } else {
                    log("Previous episode is null, can't STOP")
                }

                log("STARTING ${episode.title}")
                insert(
                    SessionAction(
                        episode = episode,
                        sessionId = sessionId,
                        action = Action.START,
                        playbackSpeed = speed,
                    ),
                )
            }
            previousEpisode = episode
            previousIsPlaying = true
            previousSpeed = speed
        }
    }

    suspend fun episodeStop(
        episode: Episode,
        sessionId: String,
        speed: Float,
    ) {
        mutex.withLock {
            if (previousEpisode?.id == episode.id &&
                previousIsPlaying == false
            ) {
                return
            }
            log("STOPPING ${previousEpisode?.title}, should be same as ${episode.title}")
            insert(
                SessionAction(
                    episode = episode,
                    sessionId = sessionId,
                    action = Action.STOP,
                    playbackSpeed = speed,
                ),
            )
            previousEpisode = episode
            previousIsPlaying = false
            previousSpeed = speed
        }
    }

    suspend fun speedChange(
        isPlaying: Boolean,
        sessionId: String,
        speed: Float,
    ) {
        mutex.withLock {
            if (previousSpeed == speed) {
                return
            }
            if (!isPlaying) {
                return
            }
            log("Changing speed for ${previousEpisode?.title} to $speed")
            val prevSpeed = previousSpeed
            val prevEpisode = previousEpisode
            if (prevEpisode != null && prevSpeed != null) {
                if (isPlaying) {
                    insert(
                        SessionAction(
                            episode = prevEpisode,
                            sessionId = sessionId,
                            action = Action.STOP,
                            playbackSpeed = prevSpeed,
                        ),
                    )
                }
                insert(
                    SessionAction(
                        episode = prevEpisode,
                        sessionId = sessionId,
                        action = Action.START,
                        playbackSpeed = speed,
                    ),
                )
            } else {
                log("Previous episode or previous speed is null, can't STOP+START for speed change action")
            }
            previousIsPlaying = true
            previousSpeed = speed
        }
    }

    private suspend fun insert(action: SessionAction) {
        sessionActionDao.insert(action)
    }

    private fun log(message: String) {
        println("Session: $message")
    }
}
