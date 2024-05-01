package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
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
    // Should be called via PodcastsAndEpisodesRepository because that does other things like marking podcasts
    // having new episodes
    suspend fun refreshForPodcastId(
        podcastId: Long,
        episodesToLoad: Long = 100,
        fetchSinceMostRecentEpisode: Boolean = true,
    ): PodcastResult<List<Episode>> {
        val fetchSinceTime =
            if (fetchSinceMostRecentEpisode) {
                episodesDao
                    .getMaxDatePublished(podcastId)
                    // Subtract an hour so that if podcasts were published close to each other, they don't get missed
                    ?.minus(1.hours.inWholeSeconds)
            } else {
                null
            }
        val request =
            GetEpisodesRequest(
                id = podcastId,
                sinceEpochSeconds = fetchSinceTime,
                max = episodesToLoad,
            )
        return when (val result = episodesApi.getByPodcastId(request)) {
            is PodcastResult.Success -> {
                val inserted = episodesDao.insert(result.data.items.map { Episode(it) })
                PodcastResult.Success(inserted)
            }

            is PodcastResult.Failure -> {
                LogHelper.v(TAG, "Failed to refresh podcast: ${result.error.exceptionMessage()}")
                PodcastResult.Failure(result.error)
            }
        }
    }

    fun getEpisodesForPodcastFlow(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): Flow<List<Episode>> {
        return episodesDao
            .getEpisodesForPodcastFlow(podcastId, sortOrder, page, showCompleted)
            .map { list ->
                list.map { dbEpisode ->
                    Episode(dbEpisode)
                }
            }
    }

    suspend fun getEpisodesForPodcast(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): List<Episode> {
        return episodesDao
            .getEpisodesForPodcast(podcastId, sortOrder, page, showCompleted)
            .map { dbEpisode ->
                Episode(dbEpisode)
            }
    }

    fun getEpisodesForPodcastsFlow(
        podcastIds: List<Long>,
        page: Long,
        showCompleted: Boolean,
    ): Flow<List<Episode>> {
        return episodesDao
            .getEpisodesForPodcastsFlow(podcastIds, page, showCompleted)
            .map { list ->
                list.map { dbEpisode ->
                    Episode(dbEpisode)
                }
            }
    }

    fun getEpisodeFlow(id: String): Flow<Episode?> {
        return episodesDao
            .getEpisodeFlow(id)
            .map { dbEpisode ->
                dbEpisode?.let { Episode(it) }
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
                list.map { dbEpisode ->
                    Episode(dbEpisode)
                }
            }
    }

    suspend fun getQueue(): List<Episode> {
        return episodesDao
            .getQueue()
            .map { dbEpisode ->
                Episode(dbEpisode)
            }
    }

    fun getDownloadedFlow(): Flow<List<Episode>> {
        return episodesDao
            .getDownloadedFlow()
            .map { list ->
                list.map { dbEpisode ->
                    Episode(dbEpisode)
                }
            }
    }

    fun getFavoritesFlow(): Flow<List<Episode>> {
        return episodesDao
            .getFavoritesFlow()
            .map { list ->
                list.map { dbEpisode ->
                    Episode(dbEpisode)
                }
            }
    }

    suspend fun getNeedDownloadEpisodes(): List<Episode> {
        return episodesDao
            .getNeedDownloadEpisodes()
            .map { dbEpisode ->
                Episode(dbEpisode)
            }
    }

    suspend fun getEpisodesEligibleForRemoval(
        removeCompletedAfter: RemoveDownloadsAfter,
        removeUnfinishedAfter: RemoveDownloadsAfter,
        now: Instant,
    ): List<Episode> {
        return episodesDao
            .getDownloaded()
            .map { dbEpisode ->
                Episode(dbEpisode)
            }
            .filter { episode ->
                val downloadAtTime = episode.downloadedAt ?: return@filter false
                if (episode.isCompleted) {
                    now.minus(downloadAtTime) >= removeCompletedAfter.duration
                } else {
                    now.minus(downloadAtTime) >= removeUnfinishedAfter.duration
                }
            }
    }

    suspend fun getAvailableEpisodeCount(podcastId: Long): Long {
        return episodesDao.getEpisodeCount(podcastId)
    }

    suspend fun getAvailableEpisodeCount(podcastIds: List<Long>): Long {
        return episodesDao.getEpisodeCount(podcastIds)
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

    suspend fun updateNeedsDownload(
        id: String,
        needsDownload: Boolean,
    ) {
        episodesDao.updateNeedsDownload(id, needsDownload)
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

    companion object {
        private const val TAG = "EpisodesRepo"
    }
}
