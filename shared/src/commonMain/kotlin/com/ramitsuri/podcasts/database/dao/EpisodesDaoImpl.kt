package com.ramitsuri.podcasts.database.dao

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ramitsuri.podcasts.DbEpisode
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntity
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntityQueries
import com.ramitsuri.podcasts.EpisodeEntity
import com.ramitsuri.podcasts.EpisodeEntityQueries
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class EpisodesDaoImpl(
    private val episodeEntityQueries: EpisodeEntityQueries,
    private val episodeAdditionalInfoEntityQueries: EpisodeAdditionalInfoEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : EpisodesDao {
    override suspend fun insert(episodes: List<Episode>): List<Episode> {
        return withContext(ioDispatcher) {
            val insertedEpisodes = mutableListOf<Episode>()
            episodes.map {
                launch {
                    if (insert(it)) {
                        insertedEpisodes.add(it)
                    }
                }
            }.joinAll()
            insertedEpisodes
        }
    }

    override fun getEpisodesForPodcastsFlow(
        podcastIds: List<Long>,
        page: Long,
    ): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getEpisodesForPodcasts(podcastIds = podcastIds, limit = page.toLimit)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override fun getEpisodesForPodcastFlow(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): Flow<List<DbEpisode>> {
        return getForPodcast(podcastId, sortOrder, page, showCompleted)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getEpisodesForPodcast(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): List<DbEpisode> {
        return withContext(ioDispatcher) {
            getForPodcast(podcastId, sortOrder, page, showCompleted)
                .executeAsList()
        }
    }

    override suspend fun getMaxDatePublished(podcastId: Long): Long? {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getMaxEpisodeDatePublishedForPodcast(podcastId)
                .executeAsOneOrNull()
                ?.maxDatePublished
        }
    }

    override fun getEpisodeFlow(id: String): Flow<DbEpisode?> {
        return episodeEntityQueries
            .getEpisode(id)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
    }

    override suspend fun getEpisode(id: String): DbEpisode? {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisode(id)
                .executeAsOneOrNull()
        }
    }

    override fun getQueueFlow(): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getEpisodesInQueue()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override fun getDownloadedFlow(): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getDownloadedEpisodes()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getDownloaded(): List<DbEpisode> {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getDownloadedEpisodes()
                .executeAsList()
        }
    }

    override fun getFavoritesFlow(): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getFavoriteEpisodes()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getQueue(): List<DbEpisode> {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisodesInQueue()
                .executeAsList()
        }
    }

    override suspend fun getNeedDownloadEpisodes(): List<DbEpisode> {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getNeedDownloadEpisodes()
                .executeAsList()
        }
    }

    override suspend fun getEpisodeCount(podcastId: Long): Long {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisodeCountForPodcast(podcastId)
                .executeAsOneOrNull()
                ?: 0
        }
    }

    override suspend fun getEpisodeCount(podcastIds: List<Long>): Long {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisodeCountForPodcasts(podcastIds)
                .executeAsOneOrNull()
                ?: 0
        }
    }

    override suspend fun updatePlayProgress(
        id: String,
        playProgressInSeconds: Int,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updatePlayProgress(id = id, playProgress = playProgressInSeconds)
        }
    }

    override suspend fun updateDownloadStatus(
        id: String,
        downloadStatus: DownloadStatus,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateDownloadStatus(id = id, downloadStatus = downloadStatus)
        }
    }

    override suspend fun updateDownloadProgress(
        id: String,
        progress: Double,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateDownloadProgress(id = id, downloadProgress = progress)
        }
    }

    override suspend fun updateDownloadBlocked(
        id: String,
        blocked: Boolean,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateDownloadBlocked(id = id, downloadBlocked = blocked)
        }
    }

    override suspend fun updateDownloadedAt(
        id: String,
        downloadedAt: Instant?,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateDownloadedAt(id = id, downloadedAt = downloadedAt)
        }
    }

    override suspend fun updateQueuePositions(idToQueuePosition: Map<String, Int>) {
        withContext(ioDispatcher) {
            episodeEntityQueries.transaction {
                idToQueuePosition.forEach { (id, position) ->
                    updateQueuePosition(id, position)
                }
            }
        }
    }

    override suspend fun addToQueue(id: String) {
        withContext(ioDispatcher) {
            val queuePosition =
                (
                    episodeAdditionalInfoEntityQueries
                        .selectMaxQueuePosition()
                        .executeAsOneOrNull()
                        ?.currentMaxQueuePosition
                        ?: Episode.NOT_IN_QUEUE
                ) + 1
            updateQueuePosition(id, queuePosition)
        }
    }

    override suspend fun updateCompletedAt(
        id: String,
        completedAt: Instant?,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateCompletedAt(id = id, completedAt = completedAt)
        }
    }

    override suspend fun updateDuration(
        id: String,
        duration: Int,
    ) {
        withContext(ioDispatcher) {
            episodeEntityQueries.updateDuration(id = id, duration = duration)
        }
    }

    override suspend fun updateFavorite(
        id: String,
        isFavorite: Boolean,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateFavorite(id = id, isFavorite = isFavorite)
        }
    }

    private fun updateQueuePosition(
        id: String,
        position: Int,
    ) {
        episodeAdditionalInfoEntityQueries.updateQueuePosition(id = id, queuePosition = position)
    }

    override suspend fun updateNeedsDownload(
        id: String,
        needsDownload: Boolean,
    ) {
        episodeAdditionalInfoEntityQueries.updateNeedsDownload(id = id, needsDownload = needsDownload)
    }

    private fun insert(episode: Episode): Boolean {
        // Always insert episode data because something might have changed
        episodeEntityQueries.insertOrReplace(
            EpisodeEntity(
                id = episode.id,
                podcastId = episode.podcastId,
                title = episode.title,
                description = episode.description,
                link = episode.link,
                enclosureUrl = episode.enclosureUrl,
                datePublished = episode.datePublished,
                duration = episode.duration,
                explicit = episode.explicit,
                episode = episode.episode,
                season = episode.season,
            ),
        )
        // Don't insert additional data because we maintain this and if we already have something, we're going to update
        // it separately, not insert anything we get
        if (episodeAdditionalInfoEntityQueries.hasId(episode.id).executeAsOne() > 0) {
            return false
        }
        episodeAdditionalInfoEntityQueries.insertOrIgnore(
            EpisodeAdditionalInfoEntity(
                id = episode.id,
                playProgress = episode.progressInSeconds,
                downloadStatus = episode.downloadStatus,
                downloadProgress = episode.downloadProgress,
                downloadBlocked = episode.downloadBlocked,
                downloadedAt = episode.downloadedAt,
                queuePosition = episode.queuePosition,
                completedAt = episode.completedAt,
                isFavorite = episode.isFavorite,
                needsDownload = episode.needsDownload,
            ),
        )
        return true
    }

    private fun getForPodcast(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
        showCompleted: Boolean,
    ): Query<DbEpisode> {
        return when (sortOrder) {
            EpisodeSortOrder.DATE_PUBLISHED_DESC -> {
                episodeEntityQueries
                    .getEpisodesForPodcast(
                        podcastId = podcastId,
                        limit = page.toLimit,
                        showCompleted = if (showCompleted) 1 else 0,
                    )
            }

            EpisodeSortOrder.DATE_PUBLISHED_ASC -> {
                episodeEntityQueries
                    .getEpisodesForPodcastAsc(
                        podcastId = podcastId,
                        limit = page.toLimit,
                        showCompleted = if (showCompleted) 1 else 0,
                    )
            }
        }
    }

    private val Long.toLimit
        get() = this * PAGE_SIZE

    // private fun Long.offset() = (this - 1) * PAGE_SIZE

    companion object {
        // TODO
        // private const val PAGE_SIZE: Long = 100
        private const val PAGE_SIZE: Long = 10
    }
}
