package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.BaseTest
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import kotlinx.coroutines.runBlocking
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodesRepositoryTest : BaseTest() {
    @Test
    fun getPodcastsThatHaveEpisodes_returnsPodcastsForWhichEpisodesExist() = runBlocking {
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

    private suspend fun insert(id: String, podcastId: Long) {
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
                    queuePosition = 0,
                    completedAt = null,
                    isFavorite = false,
                    needsDownload = false,
                ),
            ),
        )
    }
}
