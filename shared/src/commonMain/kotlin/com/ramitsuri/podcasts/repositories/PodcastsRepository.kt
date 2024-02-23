package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest

class PodcastsRepository internal constructor(
    private val podcastsApi: PodcastsApi,
    private val podcastsDao: PodcastsDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun search(request: SearchPodcastsRequest): PodcastResult<List<Podcast>> {
        return when (val apiResult = podcastsApi.search(request)) {
            is PodcastResult.Failure -> {
                apiResult
            }

            is PodcastResult.Success -> {
                PodcastResult.Success(
                    apiResult.data.podcasts
                        .map {
                            Podcast(it)
                        },
                )
            }
        }
    }

    suspend fun refreshPodcast(id: Long): Boolean {
        val result = podcastsApi.getById(id)
        return if (result is PodcastResult.Success) {
            podcastsDao.insert(listOf(Podcast(result.data.podcast)))
            true
        } else {
            false
        }
    }

    suspend fun updateSubscribed(
        id: Long,
        subscribed: Boolean,
    ) {
        podcastsDao.updateSubscribed(id, subscribed)
    }

    suspend fun updateAutoDownloadEpisodes(
        id: Long,
        autoDownloadEpisodes: Boolean,
    ) {
        podcastsDao.updateAutoDownloadEpisodes(id, autoDownloadEpisodes)
    }

    suspend fun updateNewEpisodeNotification(
        id: Long,
        showNewEpisodeNotification: Boolean,
    ) {
        podcastsDao.updateNewEpisodeNotification(id, showNewEpisodeNotification)
    }
}
