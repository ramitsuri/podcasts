package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.BaseTest
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class SessionHistoryRepositoryTest : BaseTest() {
    private val timeZone = TimeZone.of("America/New_York")

    @Test
    fun getEpisodes_returnsEpisodesWhereBothPodcastIdAndEpisodeIdMatch() =
        runBlocking {
            // Arrange
            insertSessionAction(
                podcastId = 1,
                episodeId = "P1-E1",
            )
            insertSessionAction(
                podcastId = 1,
                episodeId = "P1-E2",
            )
            insertSessionAction(
                podcastId = 2,
                episodeId = "P2-E1",
            )

            // Act
            val episodeIds =
                get<SessionHistoryRepository>()
                    .getEpisodes(episodeIds = listOf("P1-E1", "P2-E1"), podcastIds = listOf(1))
                    .map { it.episodeId }

            // Assert
            assertEquals(listOf("P1-E1"), episodeIds)
        }

    @Test
    fun getEpisodeHistory_shouldMerge_ifConsecutiveAndSameDay() = runBlocking {
        // Arrange
        val baseDateTime = LocalDateTime.parse("2024-01-01T12:00:00")
        insertSessionAction("P1-E1", 1, "s1", Action.START, baseDateTime)
        insertSessionAction("P1-E1", 1, "s2", Action.START, baseDateTime.plus(1.seconds))

        // Act
        val history = getHistory()

        // Assert
        assertEquals(listOf("P1-E1"), history.map { it.episode.id })
    }

    @Test
    fun getEpisodeHistory_shouldNotMergeConsecutiveEpisodes_ifNotSameDay() = runBlocking {
        // Arrange
        val baseDateTime = LocalDateTime.parse("2024-01-01T12:00:00")
        insertSessionAction("P1-E1", 1, "s1", Action.START, baseDateTime)
        insertSessionAction("P1-E1", 1, "s2", Action.START, baseDateTime.plus(1.days))

        // Act
        val history = getHistory()

        // Assert
        assertEquals(listOf("P1-E1", "P1-E1"), history.map { it.episode.id })
    }

    @Test
    fun getEpisodeHistory_shouldNotMerge_ifNotConsecutive() = runBlocking {
        // Arrange
        val baseDateTime = LocalDateTime.parse("2024-01-01T12:00:00")
        insertSessionAction("P1-E1", 1, "s1", Action.START, baseDateTime)
        insertSessionAction("P1-E2", 1, "s1", Action.START, baseDateTime.plus(1.seconds))
        insertSessionAction("P1-E1", 1, "s2", Action.START, baseDateTime.plus(2.seconds))

        // Act
        val history = getHistory()

        // Assert
        assertEquals(listOf("P1-E1", "P1-E2", "P1-E1"), history.map { it.episode.id })
    }

    private suspend fun insertSessionAction(
        episodeId: String,
        podcastId: Long,
        sessionId: String = "s1",
        action: Action = Action.STOP,
        date: LocalDateTime = LocalDateTime.parse("2024-01-01T12:00:00"),
    ) {
        get<PodcastsDao>().insert(listOf(podcast(id = podcastId, subscribed = true)))

        get<EpisodesDao>().insert(listOf(episode(id = episodeId, podcastId = podcastId)))

        get<SessionActionDao>().insert(
            SessionAction(
                sessionId = sessionId,
                podcastId = podcastId,
                episodeId = episodeId,
                time = date.toInstant(timeZone),
                action = action,
                playbackSpeed = 0.0f,
            ),
        )
    }

    private fun LocalDateTime.plus(duration: Duration): LocalDateTime {
        return this.toInstant(timeZone).plus(duration).toLocalDateTime(timeZone)
    }

    private suspend fun getHistory() =
        get<SessionHistoryRepository>().getEpisodeHistory(timeZone).first()
}
