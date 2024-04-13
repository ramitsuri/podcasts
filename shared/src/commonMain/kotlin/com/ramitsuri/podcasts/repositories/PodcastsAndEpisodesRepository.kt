package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.download.EpisodeDownloader
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
    private val episodeDownloader: EpisodeDownloader,
) {
    suspend fun refreshPodcast(
        podcastId: Long,
        podcastAllowsAutoDownload: Boolean = false,
        podcastAllowsAutoAddToQueue: Boolean = false,
    ): PodcastResult<List<Episode>> {
        val result = episodesRepository.refreshForPodcastId(podcastId)
        val episodes = (result as? PodcastResult.Success)?.data ?: listOf()
        podcastsRepository.updateHasNewEpisodes(podcastId, episodes.isNotEmpty())
        if (podcastAllowsAutoDownload) {
            episodes.forEach { episode ->
                episodesRepository.updateNeedsDownload(id = episode.id, needsDownload = true)
            }
        }
        if (podcastAllowsAutoAddToQueue) {
            episodes.forEach { episode ->
                episodesRepository.addToQueue(episode.id)
            }
        }
        return result
    }

    suspend fun refreshPodcasts(
        fetchFromNetwork: Boolean,
        systemAllowsAutoDownload: Boolean = false,
    ): PodcastResult<Unit> {
        return withContext(ioDispatcher) {
            val failures = mutableListOf<PodcastResult.Failure>()
            if (fetchFromNetwork) {
                val subscribed = podcastsRepository.getAllSubscribed()
                subscribed.map { podcast ->
                    launch {
                        val refreshResult =
                            refreshPodcast(
                                podcastId = podcast.id,
                                podcastAllowsAutoDownload = podcast.autoDownloadEpisodes,
                                podcastAllowsAutoAddToQueue = podcast.autoAddToQueue,
                            )
                        if (refreshResult is PodcastResult.Failure) {
                            failures.add(refreshResult)
                        }
                    }
                }.joinAll()
            }
            if (systemAllowsAutoDownload) {
                episodesRepository
                    .getNeedDownloadEpisodes()
                    .forEach { episode ->
                        episodeDownloader.add(episode)
                    }
            }
            val failure = failures.firstOrNull()
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
