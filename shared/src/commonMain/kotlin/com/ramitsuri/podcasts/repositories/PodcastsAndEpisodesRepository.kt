package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.Category
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.PodcastWithEpisodes
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

internal class PodcastsAndEpisodesRepository internal constructor(
    private val podcastsApi: PodcastsApi,
    private val podcastsDao: PodcastsDao,
    private val episodesDao: EpisodesDao,
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val podcastsRepository: PodcastsRepository,
    private val episodesRepository: EpisodesRepository,
    private val clock: Clock,
) {
    suspend fun refreshPodcasts(sinceEpochSecondsIfNeverRefreshed: Long) {
        withContext(ioDispatcher) {
            val subscribed = podcastsDao.getAllSubscribed()
            subscribed.forEach {
                launch {
                    episodesRepository.refreshForPodcastId(
                        podcastId = it.id,
                        sinceEpochSeconds = it.lastRefreshDate?.epochSeconds ?: sinceEpochSecondsIfNeverRefreshed,
                    )
                }
            }
        }
    }

    suspend fun getPodcastWithEpisodes(podcastId: Long): Flow<PodcastWithEpisodes?> {
        val podcastFromDb = podcastsDao.get(podcastId)
        val (categoryIds, refreshSinceTime) =
            if (podcastFromDb == null) { // Doesn't exist in db so refresh
                podcastsRepository.refreshPodcast(podcastId)
                podcastsDao.get(podcastId)
                val networkResult = podcastsApi.getById(podcastId)
                if (networkResult is PodcastResult.Success) {
                    podcastsDao.insert(listOf(Podcast(networkResult.data.podcast)))
                    Pair(networkResult.data.podcast.categories.map { it.id }, clock.now().minus(365.days))
                } else {
                    Pair(listOf(), clock.now())
                }
            } else {
                Pair(podcastFromDb.categories, podcastFromDb.lastRefreshDate)
            }

        episodesRepository.refreshForPodcastId(podcastId, (refreshSinceTime ?: clock.now().minus(1.days)).epochSeconds)

        val categories = categoryDao.get(categoryIds).map { Category(it.id, it.name) }

        return podcastsDao.getFlow(podcastId)
            .map { getPodcast ->
                getPodcast?.let {
                    Podcast(it, categories)
                }
            }
            .combine(
                episodesDao.getEpisodesForPodcast(podcastId).map { list ->
                    list.map { getEpisodesForPodcast ->
                        Episode(getEpisodesForPodcast)
                    }
                },
            ) { podcast, episodes ->
                if (podcast == null) {
                    null
                } else {
                    PodcastWithEpisodes(podcast, episodes)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubscribed(): Flow<List<Episode>> {
        return podcastsDao
            .getAllSubscribedFlow()
            .flatMapLatest { subscribedPodcasts ->
                val subscribedPodcastIds = subscribedPodcasts.map { it.id }
                episodesDao.getEpisodesForPodcasts(subscribedPodcastIds)
                    .map { list -> list.map { Episode(it) } }
            }
    }
}
