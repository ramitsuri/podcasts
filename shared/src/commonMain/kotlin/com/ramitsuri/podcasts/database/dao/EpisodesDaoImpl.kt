package com.ramitsuri.podcasts.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntity
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntityQueries
import com.ramitsuri.podcasts.EpisodeEntity
import com.ramitsuri.podcasts.EpisodeEntityQueries
import com.ramitsuri.podcasts.GetEpisode
import com.ramitsuri.podcasts.GetEpisodesForPodcast
import com.ramitsuri.podcasts.GetEpisodesForPodcasts
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class EpisodesDaoImpl(
    private val episodeEntityQueries: EpisodeEntityQueries,
    private val episodeAdditionalInfoEntityQueries: EpisodeAdditionalInfoEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : EpisodesDao {
    override suspend fun insert(episodes: List<Episode>) {
        withContext(ioDispatcher) {
            episodes.forEach {
                insert(it)
            }
        }
    }

    override fun getEpisodesForPodcastsFlow(podcastIds: List<Long>): Flow<List<GetEpisodesForPodcasts>> {
        return episodeEntityQueries
            .getEpisodesForPodcasts(podcastIds)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<GetEpisodesForPodcast>> {
        return episodeEntityQueries
            .getEpisodesForPodcast(podcastId)
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getEpisodesForPodcast(podcastId: Long): List<GetEpisodesForPodcast> {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisodesForPodcast(podcastId)
                .executeAsList()
        }
    }

    override fun getEpisodeFlow(id: String): Flow<GetEpisode?> {
        return episodeEntityQueries
            .getEpisode(id)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
    }

    override suspend fun getEpisode(id: String): GetEpisode? {
        return withContext(ioDispatcher) {
            episodeEntityQueries
                .getEpisode(id)
                .executeAsOneOrNull()
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

    override suspend fun updateQueuePosition(
        id: String,
        position: Int,
    ) {
        withContext(ioDispatcher) {
            episodeAdditionalInfoEntityQueries.updateQueuePosition(id = id, queuePosition = position)
        }
    }

    override suspend fun addToQueue(id: String) {
        withContext(ioDispatcher) {
            val queuePosition = (episodeAdditionalInfoEntityQueries
                .selectMaxQueuePosition()
                .executeAsOneOrNull()
                ?.currentMaxQueuePosition
                ?: Episode.NOT_IN_QUEUE) + 1
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

    private fun insert(episode: Episode) {
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
        episodeAdditionalInfoEntityQueries.insertOrIgnore(
            EpisodeAdditionalInfoEntity(
                id = episode.id,
                playProgress = episode.progressInSeconds,
                downloadStatus = episode.downloadStatus,
                downloadProgress = episode.downloadProgress,
                downloadBlocked = episode.downloadBlocked,
                queuePosition = episode.queuePosition,
                completedAt = episode.completedAt,
            ),
        )
    }
}
