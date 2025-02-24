package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.Category
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.ImportedPodcast
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastError
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PodcastsRepository internal constructor(
    private val podcastsApi: PodcastsApi,
    private val podcastsDao: PodcastsDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun get(id: Long): Podcast? {
        val dbPodcast =
            podcastsDao
                .get(id)
        return if (dbPodcast == null) {
            null
        } else {
            val categories = categoryDao.get(dbPodcast.categories).map { Category(it.id, it.name) }
            Podcast(dbPodcast, categories)
        }
    }

    fun getFlow(id: Long): Flow<Podcast?> {
        return podcastsDao
            .getFlow(id)
            .map { dbPodcast ->
                if (dbPodcast == null) {
                    null
                } else {
                    val categories = categoryDao.get(dbPodcast.categories).map { Category(it.id, it.name) }
                    Podcast(dbPodcast, categories)
                }
            }
    }

    suspend fun getAllSubscribed(): List<Podcast> {
        return podcastsDao.getAllSubscribed()
            .map { dbPodcast ->
                val categories =
                    categoryDao.get(dbPodcast.categories).map { Category(it.id, it.name) }
                Podcast(dbPodcast, categories)
            }
    }

    fun getAllSubscribedFlow(): Flow<List<Podcast>> {
        return podcastsDao.getAllSubscribedFlow()
            .map { list ->
                list.map { dbPodcast ->
                    val categories =
                        categoryDao.get(dbPodcast.categories).map { Category(it.id, it.name) }
                    Podcast(dbPodcast, categories)
                }
            }
    }

    suspend fun getAllUnsubscribed(): List<Long> {
        return podcastsDao.getAllSubscribed(subscribed = false)
            .map { dbPodcast ->
                dbPodcast.id
            }
    }

    suspend fun search(
        request: SearchPodcastsRequest,
        saveSearchResults: Boolean = true,
    ): PodcastResult<List<Podcast>> {
        return when (val apiResult = podcastsApi.search(request)) {
            is PodcastResult.Failure -> {
                apiResult
            }

            is PodcastResult.Success -> {
                val podcasts =
                    apiResult.data.podcasts
                        .map {
                            Podcast(it)
                        }
                if (saveSearchResults) {
                    podcasts.forEach { saveToDb(it) }
                }
                PodcastResult.Success(podcasts)
            }
        }
    }

    suspend fun getPodcastByUrlOrName(
        url: String,
        name: String,
    ): ImportedPodcast {
        val byUrlResult = podcastsApi.getByUrl(url)
        if (byUrlResult is PodcastResult.Success) {
            return ImportedPodcast(byUrl = Podcast(byUrlResult.data.podcast))
        } else if ((byUrlResult as PodcastResult.Failure).error is PodcastError.BadRequest) {
            // Get podcast by url returns bad request if a podcast is not found by url
            // So, do an exact search for the podcast
            val byNameResult =
                podcastsApi.search(SearchPodcastsRequest(term = name, maxResults = 1, findSimilar = false))
            val byName = (byNameResult as? PodcastResult.Success)?.data?.podcasts?.firstOrNull()
            if (byName != null) {
                return ImportedPodcast(byName = Podcast(byName))
            }
        }
        return ImportedPodcast()
    }

    suspend fun saveToDb(podcast: Podcast) {
        podcastsDao.insert(listOf(podcast))
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

    suspend fun updateHasNewEpisodes(
        id: Long,
        hasNewEpisodes: Boolean,
    ) {
        podcastsDao.updateHasNewEpisodes(id, hasNewEpisodes)
    }

    suspend fun updateAutoAddToQueueEpisodes(
        id: Long,
        autoAddToQueue: Boolean,
    ) {
        podcastsDao.updateAutoAddToQueueEpisodes(id, autoAddToQueue)
    }

    suspend fun updateShowCompletedEpisodes(
        id: Long,
        showCompletedEpisodes: Boolean,
    ) {
        podcastsDao.updateShowCompletedEpisodes(id, showCompletedEpisodes)
    }

    suspend fun updateEpisodeSortOrder(
        id: Long,
        episodeSortOrder: EpisodeSortOrder,
    ) {
        podcastsDao.updateEpisodeSortOrder(id, episodeSortOrder)
    }

    suspend fun remove(podcastIds: List<Long>) {
        podcastsDao.remove(podcastIds)
    }

    suspend fun load(id: Long) {
        (podcastsApi.getById(id) as? PodcastResult.Success)
            ?.data
            ?.let {
                podcastsDao.insert(listOf(Podcast(it.podcast)))
            }
    }
}
