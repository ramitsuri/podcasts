package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.BaseTest
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodesRepositoryTest : BaseTest() {
    @Test
    fun getPodcastsThatHaveEpisodes_returnsPodcastsForWhichEpisodesExist() =
        runBlocking {
            // Arrange
            insert(id = "P1-E1", podcastId = 1)
            insert(id = "P1-E2", podcastId = 1)
            insert(id = "P2-E1", podcastId = 2)
            insert(id = "P2-E2", podcastId = 2)

            // Act
            val result = get<EpisodesRepository>().getPodcastsThatHaveEpisodes(listOf(1, 3))

            // Assert
            assertEquals(listOf(1L), result)
        }

    @Test
    fun testSwapQueuePositions() =
        runBlocking {
            // Arrange
            insert(id = "1", queuePosition = 1)
            insert(id = "2", queuePosition = 2)
            insert(id = "3", queuePosition = 3)
            insert(id = "4", queuePosition = 4)
            insert(id = "5", queuePosition = 5)

            // Act
            // Assert
            assertQueueOrder("1", "2", "3", "4", "5")

            swapQueuePositions("1", "2")
            assertQueueOrder("2", "1", "3", "4", "5")

            swapQueuePositions("3", "4")
            assertQueueOrder("2", "1", "4", "3", "5")

            swapQueuePositions("1", "5")
            assertQueueOrder("2", "5", "4", "3", "1")

            swapQueuePositions("2", "1")
            assertQueueOrder("1", "5", "4", "3", "2")

            swapQueuePositions("1", "2")
            assertQueueOrder("2", "5", "4", "3", "1")

            swapQueuePositions("5", "4")
            assertQueueOrder("2", "4", "5", "3", "1")
        }

    @Test
    fun testSimultaneousAddToQueue(): Unit =
        runBlocking {
            // Arrange
            val repository = get<EpisodesRepository>()
            insert(id = "1", queuePosition = Episode.NOT_IN_QUEUE)
            insert(id = "2", queuePosition = Episode.NOT_IN_QUEUE)

            // Act
            val job1 =
                launch {
                    repository.addToQueue(id = "1")
                }
            val job2 =
                launch {
                    repository.addToQueue(id = "2")
                }
            joinAll(job1, job2)

            // Assert
            val queuePositions = repository.getQueue().map { it.queuePosition }
            assertEquals(2, queuePositions.size)
            assert(queuePositions[0] != queuePositions[1])
        }

    private suspend fun swapQueuePositions(
        id1: String,
        id2: String,
    ) {
        val repo = get<EpisodesRepository>()
        val episodes = repo.getQueue()
        val pos1 = episodes.indexOfFirst { it.id == id1 }
        val pos2 = episodes.indexOfFirst { it.id == id2 }
        val currentlyAtPosition1 = episodes[pos1]
        val currentlyAtPosition2 = episodes[pos2]
        repo.updateQueuePositions(
            currentlyAtPosition1.id,
            currentlyAtPosition2.queuePosition,
            currentlyAtPosition2.id,
            currentlyAtPosition1.queuePosition,
        )
    }

    private suspend fun assertQueueOrder(vararg ids: String) {
        val repo = get<EpisodesRepository>()
        assertEquals(ids.toList(), repo.getQueue().map { it.id })
    }

    private suspend fun insert(
        id: String,
        podcastId: Long = 1,
        queuePosition: Int = 0,
    ) {
        get<PodcastsDao>().insert(
            listOf(
                Podcast(
                    id = podcastId,
                    guid = "",
                    title = "",
                    description = "",
                    author = "",
                    owner = "",
                    url = "",
                    link = "",
                    image = "",
                    artwork = "",
                    explicit = false,
                    episodeCount = 0,
                    categories = listOf(),
                    subscribed = false,
                    autoDownloadEpisodes = false,
                    newEpisodeNotifications = false,
                    subscribedDate = null,
                    hasNewEpisodes = false,
                    autoAddToQueue = false,
                    showCompletedEpisodes = false,
                    episodeSortOrder = EpisodeSortOrder.DATE_PUBLISHED_DESC,
                ),
            ),
        )
        get<EpisodesDao>().insert(
            listOf(
                Episode(
                    id = id,
                    podcastId = podcastId,
                    podcastName = "",
                    podcastAuthor = "",
                    podcastImageUrl = "",
                    podcastLink = "",
                    podcastUrl = "",
                    title = "",
                    description = "",
                    link = "",
                    enclosureUrl = "",
                    datePublished = 0,
                    duration = null,
                    explicit = false,
                    episode = null,
                    season = null,
                    progressInSeconds = 0,
                    downloadStatus = DownloadStatus.PAUSED,
                    downloadProgress = 0.0,
                    downloadBlocked = false,
                    downloadedAt = null,
                    queuePosition = queuePosition,
                    completedAt = null,
                    isFavorite = false,
                    needsDownload = false,
                ),
            ),
        )
    }
}
