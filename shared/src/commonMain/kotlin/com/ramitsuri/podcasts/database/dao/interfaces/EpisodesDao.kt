package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.GetDownloadedEpisodes
import com.ramitsuri.podcasts.GetEpisode
import com.ramitsuri.podcasts.GetEpisodesForPodcast
import com.ramitsuri.podcasts.GetEpisodesForPodcasts
import com.ramitsuri.podcasts.GetEpisodesInQueue
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface EpisodesDao {
    suspend fun insert(episodes: List<Episode>)

    fun getEpisodesForPodcastsFlow(podcastIds: List<Long>): Flow<List<GetEpisodesForPodcasts>>

    fun getEpisodesForPodcastFlow(podcastId: Long): Flow<List<GetEpisodesForPodcast>>

    suspend fun getEpisodesForPodcast(podcastId: Long): List<GetEpisodesForPodcast>

    fun getEpisodeFlow(id: String): Flow<GetEpisode?>

    suspend fun getEpisode(id: String): GetEpisode?

    fun getQueueFlow(): Flow<List<GetEpisodesInQueue>>

    fun getDownloadedFlow(): Flow<List<GetDownloadedEpisodes>>

    suspend fun getQueue(): List<GetEpisodesInQueue>

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

    suspend fun updateQueuePositions(idToQueuePosition: Map<String, Int>)

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
}
