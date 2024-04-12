package com.ramitsuri.podcasts.database.dao

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

    override fun getEpisodesForPodcastsFlow(podcastIds: List<Long>): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getEpisodesForPodcasts(podcastIds)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<DbEpisode>> {
        return episodeEntityQueries
            .getEpisodesForPodcast(podcastId)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getEpisodesForPodcast(podcastId: Long): List<DbEpisode> {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisodesForPodcast(podcastId)
                .executeAsList()
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
            ),
        )
        return true
    }
}
