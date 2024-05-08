package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.BaseTest
import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionHistoryRepositoryTest : BaseTest() {
    @Test
    fun getEpisodes_returnsEpisodesWhereBothPodcastIdAndEpisodeIdMatch() =
        runBlocking {
            // Arrange
            insert(
                podcastId = 1,
                episodeId = "P1-E1",
            )
            insert(
                podcastId = 1,
                episodeId = "P1-E2",
            )
            insert(
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

    private suspend fun insert(
        episodeId: String,
        podcastId: Long,
    ) {
        get<SessionActionDao>().insert(
            SessionAction(
                sessionId = "s1",
                podcastId = podcastId,
                episodeId = episodeId,
                time = Instant.DISTANT_PAST,
                action = Action.STOP,
                playbackSpeed = 0.0f,
            ),
        )
    }
}
