package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.ui.PodcastDetailsViewState
import com.ramitsuri.podcasts.model.ui.PodcastWithSelectableEpisodes.Companion.mergeWithNew
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class PodcastDetailsViewModel(
    shouldRefreshPodcast: Boolean,
    private val podcastId: Long?,
    private val podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodesRepository: EpisodesRepository,
    episodeController: EpisodeController,
    private val settings: Settings,
    private val repository: PodcastsRepository,
    private val longLivingScope: CoroutineScope,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(PodcastDetailsViewState())
    val state = _state.asStateFlow()

    private var updatePodcastAndEpisodesJob: Job? = null

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
                    // Remove has new episodes marker if podcast page visited
                    repository.updateHasNewEpisodes(id = podcastId, hasNewEpisodes = false)
                }

                updatePodcastAndEpisodes()
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

    fun onSortOrderClicked() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast toggle episode sort order requested but podcast is null")
            return
        }
        longLivingScope.launch {
            val newSortOrder =
                when (podcast.episodeSortOrder) {
                    EpisodeSortOrder.DATE_PUBLISHED_DESC -> EpisodeSortOrder.DATE_PUBLISHED_ASC
                    EpisodeSortOrder.DATE_PUBLISHED_ASC -> EpisodeSortOrder.DATE_PUBLISHED_DESC
                }
            repository.updateEpisodeSortOrder(id = podcast.id, episodeSortOrder = newSortOrder)
        }
    }

    fun onEpisodeSelectionChanged(episodeId: String) {
        val newEpisodes =
            _state.value.podcastWithEpisodes?.episodes
                ?.map {
                    if (it.episode.id == episodeId) {
                        it.copy(selected = !it.selected)
                    } else {
                        it
                    }
                } ?: return
        _state.update { previousState ->
            previousState.copy(podcastWithEpisodes = previousState.podcastWithEpisodes?.copy(episodes = newEpisodes))
        }
    }

    fun onSelectAllEpisodes() {
        val newEpisodes =
            _state.value.podcastWithEpisodes?.episodes
                ?.map {
                    it.copy(selected = true)
                } ?: return
        _state.update { previousState ->
            previousState.copy(podcastWithEpisodes = previousState.podcastWithEpisodes?.copy(episodes = newEpisodes))
        }
    }

    fun onUnselectAllEpisodes() {
        val newEpisodes =
            _state.value.podcastWithEpisodes?.episodes
                ?.map {
                    it.copy(selected = false)
                } ?: return
        _state.update { previousState ->
            previousState.copy(podcastWithEpisodes = previousState.podcastWithEpisodes?.copy(episodes = newEpisodes))
        }
    }

    fun onMarkSelectedAsPlayed() {
        val selected = _state.value.podcastWithEpisodes?.episodes?.filter { it.selected } ?: return
        longLivingScope.launch {
            selected.map { selectableEpisode ->
                launch {
                    episodesRepository.markPlayed(selectableEpisode.episode.id)
                }
            }.joinAll()
            onUnselectAllEpisodes()
        }
    }

    fun onMarkSelectedAsNotPlayed() {
        val selected = _state.value.podcastWithEpisodes?.episodes?.filter { it.selected } ?: return
        longLivingScope.launch {
            selected.map { selectableEpisode ->
                launch {
                    episodesRepository.markNotPlayed(selectableEpisode.episode.id)
                }
            }.joinAll()
            onUnselectAllEpisodes()
        }
    }

    fun toggleShowCompletedEpisodes() {
        val podcast = _state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast toggle show completed episodes requested but podcast is null")
            return
        }
        longLivingScope.launch {
            val showCompletedEpisodes = podcast.showCompletedEpisodes
            repository.updateShowCompletedEpisodes(id = podcast.id, showCompletedEpisodes = !showCompletedEpisodes)
        }
    }

    fun onNextPageRequested() {
        val state = _state.value
        if (!state.hasMorePages) {
            LogHelper.v(TAG, "Podcast next page requested but no more pages")
            return
        }
        val newPage = state.page + 1
        LogHelper.d(TAG, "Episodes next page requested: $newPage")
        _state.update { it.copy(page = newPage) }
        updatePodcastAndEpisodes()
    }

    fun onLoadOlderEpisodesRequested(additionalCount: Long) {
        if (additionalCount < 0) {
            return
        }
        val podcastId = podcastId
        if (podcastId == null) {
            LogHelper.v(TAG, "Podcast load older episodes requested but podcast id is null")
            return
        }
        val currentCount = _state.value.availableEpisodeCount
        val episodesToLoadCount = currentCount + additionalCount
        longLivingScope.launch {
            _state.update { it.copy(loadingOlderEpisodes = true) }
            episodesRepository.refreshForPodcastId(
                podcastId = podcastId,
                episodesToLoad = episodesToLoadCount,
                fetchSinceMostRecentEpisode = false,
            )
            _state.update { it.copy(loadingOlderEpisodes = false) }
            onNextPageRequested()
        }
    }

    fun onSearchTermUpdated(searchTerm: String) {
        _state.update { it.copy(searchTerm = searchTerm) }
        updatePodcastAndEpisodes()
    }

    private fun updatePodcastAndEpisodes() {
        val podcastId = podcastId ?: return
        val state = _state.value
        val page = state.page
        val searchTerm = state.searchTerm
        updatePodcastAndEpisodesJob?.cancel()
        updatePodcastAndEpisodesJob =
            viewModelScope.launch {
                delay(300)
                combine(
                    podcastsAndEpisodesRepository.getPodcastWithEpisodesFlow(podcastId, page, searchTerm),
                    episodesRepository.getCurrentEpisode(),
                    settings.getPlayingStateFlow(),
                ) { podcastWithEpisodes, currentlyPlayingEpisode, playingState ->
                    Triple(podcastWithEpisodes, currentlyPlayingEpisode, playingState)
                }.collect { (podcastWithEpisodes, currentlyPlayingEpisode, playingState) ->
                    _state.update { previousState ->
                        val hasMorePages =
                            previousState.podcastWithEpisodes?.episodes?.size !=
                                podcastWithEpisodes?.episodes?.size &&
                                podcastWithEpisodes?.episodes?.size != null
                        previousState.copy(
                            hasMorePages = hasMorePages,
                            availableEpisodeCount = episodesRepository.getAvailableEpisodeCount(podcastId),
                            podcastWithEpisodes = previousState.podcastWithEpisodes.mergeWithNew(podcastWithEpisodes),
                            currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                            playingState = playingState,
                        )
                    }
                }
            }
    }

    companion object {
        private const val TAG = "PodcastDetails"
    }
}
