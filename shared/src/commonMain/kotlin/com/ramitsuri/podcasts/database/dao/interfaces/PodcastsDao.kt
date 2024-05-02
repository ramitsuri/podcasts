package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.GetAllPodcasts
import com.ramitsuri.podcasts.GetAllSubscribedPodcasts
import com.ramitsuri.podcasts.GetPodcast
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal interface PodcastsDao {
    fun getAll(): Flow<List<GetAllPodcasts>>

    suspend fun getAllSubscribed(): List<GetAllSubscribedPodcasts>

    fun getAllSubscribedFlow(): Flow<List<GetAllSubscribedPodcasts>>

    fun getFlow(id: Long): Flow<GetPodcast?>

    suspend fun get(id: Long): GetPodcast?

    suspend fun insert(podcasts: List<Podcast>)

    suspend fun updateSubscribed(
        id: Long,
        subscribed: Boolean,
        actionDate: Instant = Clock.System.now(),
    )

    suspend fun updateAutoDownloadEpisodes(
        id: Long,
        autoDownloadEpisodes: Boolean,
    )

    suspend fun updateNewEpisodeNotification(
        id: Long,
        showNewEpisodeNotification: Boolean,
    )

    suspend fun updateHasNewEpisodes(
        id: Long,
        hasNewEpisodes: Boolean,
    )

    suspend fun updateAutoAddToQueueEpisodes(
        id: Long,
        autoAddToQueue: Boolean,
    )

    suspend fun updateShowCompletedEpisodes(
        id: Long,
        showCompletedEpisodes: Boolean,
    )

    suspend fun updateEpisodeSortOrder(
        id: Long,
        episodeSortOrder: EpisodeSortOrder,
    )
}
