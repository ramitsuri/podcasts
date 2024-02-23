package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.GetEpisode
import com.ramitsuri.podcasts.GetEpisodesForPodcast
import com.ramitsuri.podcasts.GetEpisodesForPodcasts
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal interface EpisodesDao {
    suspend fun insert(episodes: List<Episode>)

    fun getEpisodesForPodcasts(podcastIds: List<Long>): Flow<List<GetEpisodesForPodcasts>>

    fun getEpisodesForPodcast(podcastId: Long): Flow<List<GetEpisodesForPodcast>>

    suspend fun getEpisode(id: String): Flow<GetEpisode?>

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

    suspend fun updateQueuePosition(
        id: String,
        position: Int,
    )

    suspend fun updateCompletedAt(
        id: String,
        completedAt: Instant,
    )
}
