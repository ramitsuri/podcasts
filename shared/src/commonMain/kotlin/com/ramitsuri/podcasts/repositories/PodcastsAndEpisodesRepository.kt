package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.PodcastWithEpisodes
import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

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
        if (episodes.isNotEmpty()) {
            podcastsRepository.updateHasNewEpisodes(podcastId, true)
        }
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
        downloaderTasksAllowed: Boolean,
        now: Instant,
        removeCompletedAfter: RemoveDownloadsAfter,
        removeUnfinishedAfter: RemoveDownloadsAfter,
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
            if (downloaderTasksAllowed) {
                // Download new episodes
                episodesRepository
                    .getNeedDownloadEpisodes()
                    .forEach { episode ->
                        episodeDownloader.add(episode)
                    }

                // Delete downloaded episodes
                episodesRepository
                    .getEpisodesEligibleForRemoval(
                        removeCompletedAfter = removeCompletedAfter,
                        removeUnfinishedAfter = removeUnfinishedAfter,
                        now = now,
                    )
                    .forEach { episode ->
                        episodeDownloader.remove(episode)
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

    suspend fun getPodcastWithEpisodesFlow(
        podcastId: Long,
        sortOrder: EpisodeSortOrder,
        page: Long,
    ): Flow<PodcastWithEpisodes?> {
        return withContext(ioDispatcher) {
            return@withContext combine(
                podcastsRepository.getFlow(podcastId),
                episodesRepository.getEpisodesForPodcastFlow(podcastId, sortOrder, page),
            ) { podcast, episodes ->
                if (podcast == null) {
                    null
                } else {
                    val filteredEpisodes =
                        if (podcast.showCompletedEpisodes) {
                            episodes
                        } else {
                            episodes.filter { !it.isCompleted }
                        }
                    PodcastWithEpisodes(podcast, filteredEpisodes)
                }
            }
        }
    }

    fun getSubscribedPodcastsFlow(): Flow<List<Podcast>> {
        return podcastsRepository.getAllSubscribedFlow()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubscribedFlow(page: Long=1): Flow<List<Episode>> {
        return podcastsRepository
            .getAllSubscribedFlow()
            .flatMapLatest { podcasts ->
                val subscribedPodcastIds = podcasts.map { it.id }
                episodesRepository.getEpisodesForPodcastsFlow(subscribedPodcastIds, page).map { list ->
                    list.filter { !it.isCompleted }
                }
            }
    }
}
