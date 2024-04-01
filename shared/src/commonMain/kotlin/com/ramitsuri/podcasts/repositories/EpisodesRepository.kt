package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

class EpisodesRepository internal constructor(
    private val episodesDao: EpisodesDao,
    private val episodesApi: EpisodesApi,
    private val settings: Settings,
) {
    suspend fun refreshForPodcastId(podcastId: Long): PodcastResult<Unit> {
        val episodes = episodesDao.getEpisodesForPodcast(podcastId)
        val fetchSinceTime =
            episodes
                .maxByOrNull { it.datePublished }
                ?.datePublished
                // Subtract an hour so that if podcasts were published close to each other, they don't get missed
                ?.minus(1.hours.inWholeSeconds)
        val request =
            GetEpisodesRequest(
                id = podcastId,
                sinceEpochSeconds = fetchSinceTime,
                max = 100,
            )
        return when (val result = episodesApi.getByPodcastId(request)) {
            is PodcastResult.Success -> {
                episodesDao.insert(result.data.items.map { Episode(it) })
                PodcastResult.Success(Unit)
            }

            is PodcastResult.Failure -> {
                PodcastResult.Failure(result.error)
            }
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
            .getEpisodeFlow(id)
            .map { getEpisode ->
                getEpisode?.let { Episode(it) }
            }
    }

    suspend fun getEpisode(id: String): Episode? {
        return episodesDao
            .getEpisode(id)
            ?.let {
                Episode(it)
            }
    }

    fun getQueueFlow(): Flow<List<Episode>> {
        return episodesDao
            .getQueueFlow()
            .map { list ->
                list.map { getEpisodesInQueue ->
                    Episode(getEpisodesInQueue)
                }
            }
    }

    fun getDownloadedFlow(): Flow<List<Episode>> {
        return episodesDao
            .getDownloadedFlow()
            .map { list ->
                list.map { getDownloadedEpisodes ->
                    Episode(getDownloadedEpisodes)
                }
            }
    }

    suspend fun getQueue(): List<Episode> {
        return episodesDao
            .getQueue()
            .map { getEpisodesInQueue ->
                Episode(getEpisodesInQueue)
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

    suspend fun updateDownloadedAt(
        id: String,
        downloadedAt: Instant? = Clock.System.now(),
    ) {
        episodesDao.updateDownloadedAt(id, downloadedAt)
    }

    suspend fun updateQueuePositions(idToPositionMap: Map<String, Int>) {
        episodesDao.updateQueuePositions(idToPositionMap)
    }

    suspend fun addToQueue(id: String) {
        episodesDao.addToQueue(id)
    }

    suspend fun removeFromQueue(id: String) {
        updateQueuePositions(mapOf(id to Episode.NOT_IN_QUEUE))
    }

    private suspend fun updateCompletedAt(
        id: String,
        completedAt: Instant? = Clock.System.now(),
    ) {
        episodesDao.updateCompletedAt(id, completedAt)
    }

    suspend fun updateDuration(
        id: String,
        durationInSeconds: Int,
    ) {
        episodesDao.updateDuration(id, durationInSeconds)
    }

    suspend fun updateFavorite(
        id: String,
        isFavorite: Boolean,
    ) {
        episodesDao.updateFavorite(id, isFavorite)
    }

    suspend fun markPlayed(id: String) {
        updateCompletedAt(id)
        updatePlayProgress(id, Episode.PLAY_PROGRESS_MAX)
        updateQueuePositions(mapOf(id to Episode.NOT_IN_QUEUE))
    }

    suspend fun markNotPlayed(id: String) {
        updateCompletedAt(id, null)
        updatePlayProgress(id, 0)
    }

    suspend fun getCurrentEpisode(): Flow<Episode?> {
        return settings.getCurrentEpisodeId().map { episodeId ->
            episodeId?.let { getEpisode(it) }
        }
    }

    suspend fun setCurrentlyPlayingEpisodeId(episodeId: String?) {
        settings.setCurrentlyPlayingEpisodeId(episodeId)
    }
}
