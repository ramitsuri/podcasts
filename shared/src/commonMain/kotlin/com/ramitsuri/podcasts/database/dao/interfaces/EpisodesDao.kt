package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.DbEpisode
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface EpisodesDao {
    /**
     * Returns episodes that were inserted. If additional data is not inserted but episode data is, that counts as
     * not inserted because we already knew about that episode.
     */
    suspend fun insert(episodes: List<Episode>): List<Episode>

    fun getEpisodesForPodcastsFlow(
        podcastIds: List<Long>,
        page: Long,
        showCompleted: Boolean,
    ): Flow<List<DbEpisode>>

    fun getEpisodesForPodcastFlow(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): Flow<List<DbEpisode>>

    suspend fun getEpisodesForPodcast(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): List<DbEpisode>

    suspend fun getMaxDatePublished(podcastId: Long): Long?

    fun getEpisodeFlow(id: String): Flow<DbEpisode?>

    suspend fun getEpisode(id: String): DbEpisode?

    fun getQueueFlow(): Flow<List<DbEpisode>>

    fun getDownloadedFlow(): Flow<List<DbEpisode>>

    suspend fun getDownloaded(): List<DbEpisode>

    fun getFavoritesFlow(): Flow<List<DbEpisode>>

    suspend fun getQueue(): List<DbEpisode>

    suspend fun getNeedDownloadEpisodes(): List<DbEpisode>

    suspend fun getEpisodeCount(podcastId: Long): Long

    suspend fun getEpisodeCount(podcastIds: List<Long>): Long

    suspend fun updatePlayProgress(
        id: String,
        playProgressInSeconds: Int,
    )

    suspend fun updateDownloadStatus(
        id: String,
        downloadStatus: DownloadStatus,
    )

    suspend fun updateDownloadProgress(
        id: String,
        progress: Double,
    )

    suspend fun updateDownloadBlocked(
        id: String,
        blocked: Boolean,
    )

    suspend fun updateDownloadedAt(
        id: String,
        downloadedAt: Instant?,
    )

    suspend fun updateQueuePosition(
        id: String,
        position: Int,
    )

    suspend fun updateQueuePositions(
        id1: String,
        position1: Int,
        id2: String,
        position2: Int,
    )

    suspend fun updateFavorite(
        id: String,
        isFavorite: Boolean,
    )

    suspend fun addToQueue(id: String)

    suspend fun updateCompletedAt(
        id: String,
        completedAt: Instant?,
    )

    suspend fun updateDuration(
        id: String,
        duration: Int,
    )

    suspend fun updateNeedsDownload(
        id: String,
        needsDownload: Boolean,
    )

    suspend fun getRemovableEpisodes(forPodcastIds: List<Long>): List<EpisodeAndPodcastId>

    suspend fun getPodcastIdsThatHaveEpisodes(podcastIds: List<Long>): List<Long>

    suspend fun remove(episodeIds: List<String>)
}
