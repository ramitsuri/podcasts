package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class EpisodesRepository internal constructor(
    private val episodesDao: EpisodesDao,
    private val episodesApi: EpisodesApi,
) {
    suspend fun refreshForPodcastId(podcastId: Long): Boolean {
        val episodes = episodesDao.getEpisodesForPodcast(podcastId)
        val fetchSinceTime =
            episodes
                .maxByOrNull { it.datePublished }
                ?.datePublished
        val request =
            GetEpisodesRequest(
                id = podcastId,
                sinceEpochSeconds = fetchSinceTime,
                max = 100,
            )
        val result = episodesApi.getByPodcastId(request)
        return if (result is PodcastResult.Success) {
            episodesDao.insert(result.data.items.map { Episode(it) })
            true
        } else {
            false
        }
    }

    fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<Episode>> {
        return episodesDao
            .getEpisodesForPodcastFlow(podcastId)
            .map { list ->
                list.map { getEpisodesForPodcast ->
                    Episode(getEpisodesForPodcast)
                }
            }
    }

    fun getEpisodesForPodcastsFlow(podcastIds: List<Long>): Flow<List<Episode>> {
        return episodesDao
            .getEpisodesForPodcastsFlow(podcastIds)
            .map { list ->
                list.map { getEpisodesForPodcast ->
                    Episode(getEpisodesForPodcast)
                }
            }
    }

    fun getEpisodeFlow(id: String): Flow<Episode?> {
        return episodesDao
            .getEpisode(id)
            .map { getEpisode ->
                getEpisode?.let { Episode(it) }
            }
    }

    suspend fun updatePlayProgress(
        id: String,
        playProgressInSeconds: Int,
    ) {
        episodesDao.updatePlayProgress(id, playProgressInSeconds)
    }

    suspend fun updateDownloadStatus(
        id: String,
        downloadStatus: DownloadStatus,
    ) {
        episodesDao.updateDownloadStatus(id, downloadStatus)
    }

    suspend fun updateDownloadProgress(
        id: String,
        progress: Double,
    ) {
        episodesDao.updateDownloadProgress(id, progress)
    }

    suspend fun updateDownloadBlocked(
        id: String,
        blocked: Boolean,
    ) {
        episodesDao.updateDownloadBlocked(id, blocked)
    }

    suspend fun updateQueuePosition(
        id: String,
        position: Int,
    ) {
        episodesDao.updateQueuePosition(id, position)
    }

    suspend fun updateCompletedAt(
        id: String,
        completedAt: Instant,
    ) {
        episodesDao.updateCompletedAt(id, completedAt)
    }
}
