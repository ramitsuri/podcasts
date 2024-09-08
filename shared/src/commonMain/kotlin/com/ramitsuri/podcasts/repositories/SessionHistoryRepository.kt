package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
import com.ramitsuri.podcasts.model.SessionAction
import com.ramitsuri.podcasts.model.SessionEpisode
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
                d("STARTING ${episode.title}")
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
                    d("STOPPING ${previousEpisode?.title}")
                    insert(
                        SessionAction(
                            episode = prevEpisode,
                            sessionId = sessionId,
                            action = Action.STOP,
                            playbackSpeed = speed,
                        ),
                    )
                } else {
                    v("Previous episode is null, can't STOP")
                }

                d("STARTING ${episode.title}")
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
            d("STOPPING ${previousEpisode?.title}, should be same as ${episode.title}")
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
            d("Changing speed for ${previousEpisode?.title} to $speed")
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
                v("Previous episode or previous speed is null, can't STOP+START for speed change action")
            }
            previousIsPlaying = true
            previousSpeed = speed
        }
    }

    fun getEpisodeHistory(timeZone: TimeZone): Flow<List<SessionEpisode>> {
        return sessionActionDao
            .getSessionActionEntities()
            .map { sessionActionEntities ->
                sessionActionEntities.map { sessionActionEntity ->
                    SessionEpisode(
                        sessionActionEntity.episodeId,
                        sessionActionEntity.sessionId,
                        sessionActionEntity.time,
                    )
                }.mergeConsecutiveDuplicateEpisodes(timeZone)
            }
    }

    suspend fun getEpisodes(
        episodeIds: List<String>,
        podcastIds: List<Long>,
    ): List<EpisodeAndPodcastId> {
        return sessionActionDao.getEpisodes(episodeIds = episodeIds, podcastIds = podcastIds)
    }

    private suspend fun insert(action: SessionAction) {
        sessionActionDao.insert(action)
    }

    private fun List<SessionEpisode>.mergeConsecutiveDuplicateEpisodes(timeZone: TimeZone): List<SessionEpisode> {
        var previous: SessionEpisode? = null
        val merged = mutableListOf<SessionEpisode>()
        forEach { episodeHistory ->
            val previousEpisode = previous
            if (previousEpisode == null ||
                previousEpisode.episodeId != episodeHistory.episodeId ||
                previousEpisode.date(timeZone) != episodeHistory.date(timeZone)
            ) {
                merged.add(episodeHistory)
            }
            previous = episodeHistory
        }
        return merged
    }

    private fun SessionEpisode.date(timeZone: TimeZone): LocalDate {
        return time.toLocalDateTime(timeZone).date
    }

    private fun d(message: String) {
        LogHelper.d("SessionHistory", message)
    }

    private fun v(message: String) {
        LogHelper.v("SessionHistory", message)
    }
}
