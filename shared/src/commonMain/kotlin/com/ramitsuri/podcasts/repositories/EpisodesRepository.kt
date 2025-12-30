package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

class EpisodesRepository internal constructor(
    private val episodesDao: EpisodesDao,
    private val episodesApi: EpisodesApi,
    private val settings: Settings,
) {
    private val addToQueueMutex = Mutex()

    // Should be called via PodcastsAndEpisodesRepository when necessary because that does other things like
    // marking podcasts having new episodes
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
        searchTerm: String,
    ): List<Episode> {
        return episodesDao
            .getEpisodesForPodcast(
                podcastId = podcastId,
                sortOrder = sortOrder,
                page = page,
                showCompleted = showCompleted,
                searchTerm = searchTerm,
            )
            .map { dbEpisode ->
                Episode(dbEpisode)
            }
    }

    fun getEpisodesUpdated() = episodesDao.getEpisodesUpdatedFlow()

    suspend fun getEpisodes(episodeIds: List<String>) = episodesDao.getEpisodes(episodeIds).map { Episode(it) }

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

    fun getDownloadingFlow(): Flow<List<Episode>> {
        return episodesDao
            .getDownloadingFlow()
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

    suspend fun getEpisodesEligibleForDownloadRemoval(
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
                if (episode.completedAt != null) { // Completed episode
                    now.minus(episode.completedAt) >= removeCompletedAfter.duration
                } else { // Not completed episode
                    val downloadAtTime = episode.downloadedAt ?: return@filter false
                    now.minus(downloadAtTime) >= removeUnfinishedAfter.duration
                }
            }
    }

    suspend fun getRemovableEpisodes(podcastIds: List<Long>): List<EpisodeAndPodcastId> {
        return episodesDao.getRemovableEpisodes(podcastIds)
    }

    suspend fun getPodcastsThatHaveEpisodes(podcastIds: List<Long>): List<Long> {
        return episodesDao.getPodcastIdsThatHaveEpisodes(podcastIds)
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

    private suspend fun updateDownloadBlocked(
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

    suspend fun updateDownloadRemoved(id: String) {
        updateDownloadBlocked(id, true)
        updateDownloadStatus(id, DownloadStatus.NOT_DOWNLOADED)
        updateDownloadProgress(id, 0.0)
        updateDownloadedAt(id, null)
        updateNeedsDownload(id, false)
    }

    suspend fun updateQueuePositions(
        id1: String,
        position1: Int,
        id2: String,
        position2: Int,
    ) {
        episodesDao.updateQueuePositions(
            id1 = id1,
            position1 = position1,
            id2 = id2,
            position2 = position2,
        )
    }

    suspend fun addToQueue(id: String) {
        // Adding with mutex because it relies on the latest value already written to the db for generating next
        // episode's queue position
        addToQueueMutex.withLock {
            episodesDao.addToQueue(id)
        }
    }

    suspend fun updateQueuePosition(
        id: String,
        position: Int,
    ) {
        episodesDao.updateQueuePosition(id, position)
    }

    suspend fun removeFromQueue(id: String) {
        episodesDao.updateQueuePosition(id, Episode.NOT_IN_QUEUE)
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
        val episode = getEpisode(id)
        if (episode == null) {
            LogHelper.v(TAG, "Episode with id $id not found")
            return
        }
        updateCompletedAt(id)
        updatePlayProgress(id, episode.duration ?: Episode.PLAY_PROGRESS_MAX)
        removeFromQueue(id)
    }

    suspend fun markNotPlayed(id: String) {
        updateCompletedAt(id, null)
        updatePlayProgress(id, 0)
    }

    fun getCurrentEpisode(): Flow<Episode?> {
        return settings.getCurrentEpisodeId().map { episodeId ->
            episodeId?.let { getEpisode(it) }
        }
    }

    suspend fun setCurrentlyPlayingEpisodeId(episodeId: String?) {
        settings.setCurrentlyPlayingEpisodeId(episodeId)
    }

    suspend fun remove(episodeIds: List<String>) {
        episodesDao.remove(episodeIds)
    }

    suspend fun load(
        id: String,
        podcastId: Long,
    ): Episode? {
        val dbEpisode = episodesDao.getEpisode(id)
        if (dbEpisode != null) {
            return Episode(dbEpisode)
        }
        LogHelper.v(TAG, "Episode not available in db, loading episode with id: $id")
        val networkResponse =
            (episodesApi.getById(id = id, podcastId = podcastId) as? PodcastResult.Success)
                ?.data
        if (networkResponse == null) {
            return null
        }
        return episodesDao.insert(listOf(Episode(networkResponse.episode))).firstOrNull()
    }

    companion object {
        private const val TAG = "EpisodesRepo"
    }
}
