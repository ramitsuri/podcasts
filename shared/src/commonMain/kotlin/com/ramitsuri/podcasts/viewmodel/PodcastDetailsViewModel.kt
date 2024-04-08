package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.PodcastDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PodcastDetailsViewModel(
    shouldRefreshPodcast: Boolean,
    podcastId: Long?,
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    episodesRepository: EpisodesRepository,
    episodeController: EpisodeController,
    settings: Settings,
    private val repository: PodcastsRepository,
    private val longLivingScope: CoroutineScope,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(PodcastDetailsViewState())
    val state = _state.asStateFlow()

    init {
        if (podcastId == null) {
            LogHelper.v(TAG, "Podcast id is null")
        } else {
            viewModelScope.launch {
                if (shouldRefreshPodcast) {
                    launch {
                        podcastsAndEpisodesRepository.refreshPodcast(podcastId)
                    }
                }

                launch {
                    combine(
                        podcastsAndEpisodesRepository.getPodcastWithEpisodesFlow(podcastId),
                        episodesRepository.getCurrentEpisode(),
                        settings.getPlayingStateFlow(),
                    ) { podcastWithEpisodes, currentlyPlayingEpisode, playingState ->
                        Triple(podcastWithEpisodes, currentlyPlayingEpisode, playingState)
                    }.collect { (podcastWithEpisodes, currentlyPlayingEpisode, playingState) ->
                        _state.update {
                            it.copy(
                                podcastWithEpisodes = podcastWithEpisodes,
                                currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                                playingState = playingState,
                            )
                        }
                    }
                }

                launch {
                    // Remove has new episodes marker if podcast page visited
                    repository.updateHasNewEpisodes(id = podcastId, hasNewEpisodes = false)
                }
            }
        }
    }

    fun onSubscribeClicked() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast subscribe requested but podcast is null")
            return
        }
        longLivingScope.launch {
            repository.updateSubscribed(id = podcast.id, subscribed = true)
        }
    }

    fun onUnsubscribeClicked() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast unsubscribe requested but podcast is null")
            return
        }
        longLivingScope.launch {
            repository.updateSubscribed(id = podcast.id, subscribed = false)
        }
    }

    fun toggleAutoDownloadClicked() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast toggle auto download requested but podcast is null")
            return
        }
        longLivingScope.launch {
            val currentAutoDownload = podcast.autoDownloadEpisodes
            repository.updateAutoDownloadEpisodes(id = podcast.id, autoDownloadEpisodes = !currentAutoDownload)
        }
    }

    fun toggleAutoAddToQueueClicked() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast toggle auto add to queue requested but podcast is null")
            return
        }
        longLivingScope.launch {
            val currentAutoAddToQueue = podcast.autoAddToQueue
            repository.updateAutoAddToQueueEpisodes(id = podcast.id, autoAddToQueue = !currentAutoAddToQueue)
        }
    }

    companion object {
        private const val TAG = "PodcastDetails"
    }
}
