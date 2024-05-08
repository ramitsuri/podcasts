package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.BaseTest
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals

class PodcastsAndEpisodesRepositoryTest : BaseTest() {
    @Test
    fun `should remove if podcast notSubscribed, 1 removable episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf(), getEpisodes(podcastIds = listOf(1)))
            assertEquals(listOf(), getPodcasts())
        }

    @Test
    fun `should NOT remove if podcast notSubscribed, 1 downloaded episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = true,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E1"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L, 2L), getPodcasts().map { it.id })
        }

    @Test
    fun `should NOT remove if podcast notSubscribed, 1 inQueue episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = true,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E1"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L), getPodcasts().map { it.id })
        }

    @Test
    fun `should NOT remove if podcast notSubscribed, 1 favorite episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = true,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E1"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L), getPodcasts().map { it.id })
        }

    @Test
    fun `should NOT remove if podcast notSubscribed, 1 listenedTo episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = true,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E1"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L), getPodcasts().map { it.id })
        }

    @Test
    fun `should NOT remove if podcast subscribed, 1 removable episode`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = true,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E1"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L), getPodcasts().map { it.id })
        }

    @Test
    fun `should remove partially if podcast not subscribed, 1 removable episode, 1 not removable`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )
            insertEpisode(
                episodeId = "P1-E2",
                podcastSubscribed = false,
                downloaded = true,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf("P1-E2"), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(1L), getPodcasts().map { it.id })
        }

    @Test
    fun `should remove entirely if podcast not subscribed, 2 removable episodes`() =
        runBlocking {
            // Arrange
            insertEpisode(
                episodeId = "P1-E1",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )
            insertEpisode(
                episodeId = "P1-E2",
                podcastSubscribed = false,
                downloaded = false,
                inQueue = false,
                isFavorite = false,
                previouslyListenedTo = false,
            )

            // Act
            removeIrrelevant()

            // Assert
            assertEquals(listOf(), getEpisodes(podcastIds = listOf(1)).map { it.id })
            assertEquals(listOf(), getPodcasts().map { it.id })
        }

    private suspend fun removeIrrelevant() {
        get<PodcastsAndEpisodesRepository>().removeIrrelevantPodcastsAndEpisodes()
    }

    private suspend fun insertEpisode(
        episodeId: String,
        podcastId: Long = 1L,
        podcastSubscribed: Boolean = false,
        downloaded: Boolean,
        inQueue: Boolean,
        isFavorite: Boolean,
        previouslyListenedTo: Boolean,
    ) {
        get<PodcastsDao>().insert(
            listOf(
                podcast(id = podcastId, subscribed = podcastSubscribed),
            ),
        )
        get<EpisodesDao>().insert(
            listOf(
                episode(
                    id = episodeId,
                    podcastId = podcastId,
                    downloadStatus = if (downloaded) DownloadStatus.DOWNLOADED else DownloadStatus.NOT_DOWNLOADED,
                    queuePosition = if (inQueue) 1 else Episode.NOT_IN_QUEUE,
                    isFavorite = isFavorite,
                ),
            ),
        )
        if (previouslyListenedTo) {
            get<SessionActionDao>().insert(
                SessionAction(
                    sessionId = "S1",
                    podcastId = podcastId,
                    episodeId = episodeId,
                    time = get<Clock>().now(),
                    action = Action.START,
                    playbackSpeed = 1.0f,
                ),
            )
        }
    }

    private suspend fun getEpisodes(podcastIds: List<Long>) =
        get<EpisodesDao>().getEpisodesForPodcastsFlow(
            podcastIds = podcastIds,
            page = 10,
            showCompleted = true,
        ).first()

    private suspend fun getPodcasts() = get<PodcastsDao>().getAll().first()

    private fun podcast(
        id: Long,
        subscribed: Boolean,
    ) = Podcast(
        id = id,
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
        subscribed = subscribed,
        autoDownloadEpisodes = false,
        newEpisodeNotifications = false,
        subscribedDate = null,
        hasNewEpisodes = false,
        autoAddToQueue = false,
        showCompletedEpisodes = false,
        episodeSortOrder = EpisodeSortOrder.DATE_PUBLISHED_DESC,
    )

    private fun episode(
        id: String,
        podcastId: Long,
        downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
        queuePosition: Int = -1,
        isFavorite: Boolean = false,
    ) = Episode(
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
        downloadStatus = downloadStatus,
        downloadProgress = 0.0,
        downloadBlocked = false,
        downloadedAt = null,
        queuePosition = queuePosition,
        completedAt = null,
        isFavorite = isFavorite,
        needsDownload = false,
    )
}
