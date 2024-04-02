package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastResult
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
    suspend fun refreshPodcast(podcastId: Long): PodcastResult<Unit> {
        return when (val result = episodesRepository.refreshForPodcastId(podcastId)) {
            is PodcastResult.Failure -> {
                result
            }

            is PodcastResult.Success -> {
                if (result.data.count > 0) {
                    podcastsRepository.updateHasNewEpisodes(podcastId, true)
                }
                PodcastResult.Success(Unit)
            }
        }
    }

    suspend fun refreshPodcasts(): PodcastResult<Unit> {
        return withContext(ioDispatcher) {
            val subscribed = podcastsRepository.getAllSubscribed()
            val results = mutableListOf<PodcastResult<Unit>>()
            subscribed.map {
                launch {
                    results.add(refreshPodcast(podcastId = it.id))
                }
            }.joinAll()
            val failure = results.firstOrNull { it is PodcastResult.Failure } as? PodcastResult.Failure
            if (failure == null) {
                PodcastResult.Success(Unit)
            } else {
                PodcastResult.Failure(failure.error)
            }
        }
    }

    suspend fun subscribeToImportedPodcasts(podcasts: List<Podcast>) {
        withContext(ioDispatcher) {
            podcasts.map {
                launch {
                    podcastsRepository.saveToDb(it.copy(subscribed = true))
                    // Calling this directly here rather than via refreshPodcast because we don't really need to mark
                    // podcasts having new episodes as we've just imported them, all of the episodes are going to be new
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
