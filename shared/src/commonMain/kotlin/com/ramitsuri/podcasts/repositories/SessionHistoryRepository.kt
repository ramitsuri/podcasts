package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
import com.ramitsuri.podcasts.model.SessionAction
import com.ramitsuri.podcasts.model.SessionEpisode
import com.ramitsuri.podcasts.model.YearEndReview
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class SessionHistoryRepository internal constructor(
    private val sessionActionDao: SessionActionDao,
    private val defaultDispatcher: CoroutineDispatcher,
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

    suspend fun getReview(
        year: Int,
        timeZone: TimeZone,
    ): YearEndReview {
        return withContext(defaultDispatcher) {
            val sessions = getEpisodeSessions()

            val listeningSince =
                sessions
                    .minBy { it.startTime }
                    .startTime

            val mostListenedToPodcasts =
                sessions
                    .groupBy {
                        it.podcastId
                    }
                    .map { (podcastId, sessions) ->
                        podcastId to sessions.sumDuration()
                    }
                    .sortedBy { (_, totalDuration) ->
                        totalDuration
                    }
                    .take(3)
                    .map { (podcastId, _) ->
                        podcastId
                    }

            val totalDurationListened = sessions.sumDuration()

            val totalConsumedDuration = sessions.sumDuration(useSpeedMultiplier = true)

            val totalEpisodesListened =
                sessions
                    .map { it.episodeId }
                    .distinct()
                    .size

            val days =
                getDaysOfYear(year, timeZone)
                    .map { dayOfYear ->
                        val startBeforeCurrentDayEndInCurrentDay =
                            sessions
                                .filter { session ->
                                    session.startTime < dayOfYear.startTime &&
                                        session.endTime > dayOfYear.startTime &&
                                        session.endTime <= dayOfYear.endTime
                                }
                                .sumOf { session ->
                                    session.endTime.minus(dayOfYear.startTime)
                                }
                        val startCurrentDayEndAfterCurrentDay =
                            sessions
                                .filter { session ->
                                    session.startTime >= dayOfYear.startTime &&
                                        session.startTime < dayOfYear.endTime &&
                                        session.endTime > dayOfYear.endTime
                                }
                                .sumOf { session ->
                                    dayOfYear.endTime.minus(session.startTime)
                                }
                        val startBeforeCurrentDayEndAfterCurrentDay =
                            sessions
                                .filter { session ->
                                    session.startTime < dayOfYear.startTime && session.endTime > dayOfYear.endTime
                                }
                                .sumOf {
                                    dayOfYear.endTime.minus(dayOfYear.startTime)
                                }
                        val startCurrentDayEndCurrentDay =
                            sessions
                                .filter { session ->
                                    session.startTime >= dayOfYear.startTime && session.endTime <= dayOfYear.endTime
                                }
                                .sumDuration()
                        val totalDuration =
                            listOf(
                                startBeforeCurrentDayEndInCurrentDay,
                                startCurrentDayEndAfterCurrentDay,
                                startBeforeCurrentDayEndAfterCurrentDay,
                                startCurrentDayEndCurrentDay,
                            ).sumOf { it }
                        dayOfYear.copy(listenDuration = totalDuration)
                    }

            val mostListenedOnDayOfWeek =
                days
                    .groupBy { it.dayOfWeek }
                    .map { (dayOfWeek, days) ->
                        dayOfWeek to days.sumOf { it.listenDuration }
                    }
                    .maxBy { it.second }

            val mostListenedOnDay =
                days
                    .maxBy { it.listenDuration }
                    .let { day ->
                        LocalDate(year = year, dayOfMonth = day.dayOfMonth, month = day.month) to day.listenDuration
                    }

            val mostListenedMonth =
                days
                    .groupBy { it.month }
                    .map { (month, days) ->
                        month to days.sumOf { it.listenDuration }
                    }
                    .maxBy { it.second }

            YearEndReview(
                listeningSince = listeningSince.toLocalDateTime(timeZone),
                mostListenedToPodcasts = mostListenedToPodcasts,
                totalDurationListened = totalDurationListened,
                totalConsumedDuration = totalConsumedDuration,
                totalEpisodesListened = totalEpisodesListened,
                mostListenedOnDayOfWeek =
                    YearEndReview.MostListenedDayOfWeek(
                        dayOfWeek = mostListenedOnDayOfWeek.first,
                        duration = mostListenedOnDayOfWeek.second,
                    ),
                mostListenedDate =
                    YearEndReview.MostListenedDate(
                        date = mostListenedOnDay.first,
                        duration = mostListenedOnDay.second,
                    ),
                mostListenedMonth =
                    YearEndReview.MostListenedMonth(
                        month = mostListenedMonth.first,
                        duration = mostListenedMonth.second,
                    ),
            )
        }
    }

    fun hasSessions() = sessionActionDao.hasSessions()

    private inline fun Iterable<Session>.sumDuration(useSpeedMultiplier: Boolean = false): Duration {
        return sumOf { session ->
            val speed = (if (useSpeedMultiplier) session.playbackSpeed else 1f).toDouble()
            session.duration.times(speed)
        }
    }

    private inline fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration {
        var sum: Duration = Duration.ZERO
        for (element in this) {
            sum += selector(element)
        }
        return sum
    }

    private fun getDaysOfYear(
        year: Int,
        timeZone: TimeZone,
    ): List<Days> {
        var date = LocalDate(year, 1, 1)
        return buildList {
            while (date.year == year) {
                val start = date.atStartOfDayIn(timeZone)
                val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1.nanoseconds)
                add(
                    Days(
                        dayOfMonth = date.dayOfMonth,
                        month = date.month,
                        dayOfWeek = date.dayOfWeek,
                        startTime = start,
                        endTime = end,
                    ),
                )
                date = date.plus(1, DateTimeUnit.DAY)
            }
        }
    }

    private suspend fun getEpisodeSessions(): List<Session> {
        val sessions = mutableListOf<Session>()
        sessionActionDao
            .getAll()
            .groupBy { it.sessionId }
            // session and its actions
            .forEach { (_, entitiesForSession) ->
                entitiesForSession
                    .groupBy { it.episodeId + it.podcastId }
                    // Each episode's actions in each session
                    .forEach { (_, entitiesForEpisode) ->
                        val (startEntities, stopEntities) = entitiesForEpisode.partition { it.action == Action.START }
                        if (startEntities.size != stopEntities.size) {
                            LogHelper.v("SessionHistoryRepository", "startEntities.size!=stopEntities.size")
                        }
                        var index = 0
                        while (index < startEntities.size && index < stopEntities.size) {
                            val start = startEntities[index]
                            val stop = stopEntities[index]
                            sessions.add(
                                Session(
                                    episodeId = start.episodeId,
                                    podcastId = start.podcastId,
                                    playbackSpeed = start.playbackSpeed,
                                    startTime = start.time,
                                    endTime = stop.time,
                                ),
                            )
                            index++
                        }
                    }
            }
        return sessions
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

    private data class Session(
        val episodeId: String,
        val podcastId: Long,
        val playbackSpeed: Float,
        val startTime: Instant,
        val endTime: Instant,
    ) {
        val duration: Duration
            get() = endTime.minus(startTime)
    }

    private data class Days(
        val dayOfMonth: Int,
        val month: Month,
        val dayOfWeek: DayOfWeek,
        val startTime: Instant,
        val endTime: Instant,
        val listenDuration: Duration = Duration.ZERO,
    )
}
