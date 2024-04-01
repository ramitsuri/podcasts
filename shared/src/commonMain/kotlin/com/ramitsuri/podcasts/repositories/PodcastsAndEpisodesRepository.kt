package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastWithEpisodes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastsAndEpisodesRepository internal constructor(
    private val podcastsRepository: PodcastsRepository,
    private val episodesRepository: EpisodesRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun refreshPodcasts() {
        withContext(ioDispatcher) {
            val subscribed = podcastsRepository.getAllSubscribed()
            subscribed.map {
                launch {
                    episodesRepository.refreshForPodcastId(podcastId = it.id)
                }
            }.joinAll()
        }
    }

    suspend fun subscribeToImportedPodcasts(podcasts: List<Podcast>) {
        withContext(ioDispatcher) {
            podcasts.map {
                launch {
                    podcastsRepository.saveToDb(it.copy(subscribed = true))
                    episodesRepository.refreshForPodcastId(it.id)
                }
            }.joinAll()
        }
    }

    suspend fun getPodcastWithEpisodesFlow(podcastId: Long): Flow<PodcastWithEpisodes?> {
        return withContext(ioDispatcher) {
            return@withContext combine(
                podcastsRepository.getFlow(podcastId),
                episodesRepository.getEpisodesForPodcastFlow(podcastId),
            ) { podcast, episodes ->
                if (podcast == null) {
                    null
                } else {
                    PodcastWithEpisodes(podcast, episodes)
                }
            }
        }
    }

    fun getSubscribedPodcastsFlow(): Flow<List<Podcast>> {
        return podcastsRepository.getAllSubscribedFlow()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubscribedFlow(): Flow<List<Episode>> {
        return podcastsRepository
            .getAllSubscribedFlow()
            .flatMapLatest { podcasts ->
                val subscribedPodcastIds = podcasts.map { it.id }
                episodesRepository.getEpisodesForPodcastsFlow(subscribedPodcastIds).map { list ->
                    list.filter { !it.isCompleted }
                }
            }
    }
}
