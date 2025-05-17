package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.ui.PodcastDetailsViewState
import com.ramitsuri.podcasts.model.ui.PodcastWithSelectableEpisodes
import com.ramitsuri.podcasts.model.ui.SelectableEpisode
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    private val page = MutableStateFlow(1L)
    private val selections = MutableStateFlow(listOf<String>())
    private val loadingOlderEpisodes = MutableStateFlow(false)
    private val searchTerm = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state =
        combine(
            page,
            searchTerm,
        ) { page, searchTerm ->
            page to searchTerm
        }.flatMapLatest { (page, searchTerm) ->
            if (podcastId == null) {
                LogHelper.v(TAG, "Podcast id is null")
                flowOf(PodcastDetailsViewState())
            } else {
                combine(
                    podcastsAndEpisodesRepository.getPodcastWithEpisodesFlow(podcastId, page, searchTerm),
                    episodesRepository.getCurrentEpisode(),
                    settings.getPlayingStateFlow(),
                    selections,
                    loadingOlderEpisodes,
                ) { podcastWithEpisodes, currentlyPlayingEpisode, playingState, selections, loadingOlderEpisodes ->
                    if (podcastWithEpisodes == null) {
                        LogHelper.v(TAG, "Podcast with episodes is null")
                        PodcastDetailsViewState()
                    } else {
                        val podcastWithSelectableEpisodes =
                            podcastWithEpisodes.let { podcast ->
                                val selectableEpisodes =
                                    podcast.episodes.map { episode ->
                                        SelectableEpisode(
                                            episode = episode,
                                            selected = selections.contains(episode.id),
                                        )
                                    }
                                PodcastWithSelectableEpisodes(podcast.podcast, selectableEpisodes)
                            }
                        val availableEpisodeCount = episodesRepository.getAvailableEpisodeCount(podcastId)
                        val hasMorePages = availableEpisodeCount != podcastWithEpisodes.episodes.size.toLong()
                        PodcastDetailsViewState(
                            podcastWithEpisodes = podcastWithSelectableEpisodes,
                            currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                            playingState = playingState,
                            page = page,
                            availableEpisodeCount = availableEpisodeCount,
                            hasMorePages = hasMorePages,
                            loadingOlderEpisodes = loadingOlderEpisodes,
                            searchTerm = searchTerm,
                        )
                    }
                }.onEach {
                    LogHelper.v(
                        TAG,
                        "State updated: ${it.podcastWithEpisodes?.podcast?.title}, " +
                            "${it.podcastWithEpisodes?.episodes?.size} episodes",
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PodcastDetailsViewState(),
        )

    init {
        LogHelper.d(TAG, "Navigated to with podcast id: $podcastId, should refresh: $shouldRefreshPodcast")
        if (podcastId == null) {
            LogHelper.v(TAG, "Podcast id is null, cannot refresh")
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
            }
        }
    }

    fun onSubscribeClicked() {
        val podcast = state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast subscribe requested but podcast is null")
            return
        }
        longLivingScope.launch {
            repository.updateSubscribed(id = podcast.id, subscribed = true)
        }
    }

    fun onUnsubscribeClicked() {
        val podcast = state.value.podcastWithEpisodes?.podcast
        if (podcast == null) {
            LogHelper.v(TAG, "Podcast unsubscribe requested but podcast is null")
            return
        }
        longLivingScope.launch {
            repository.updateSubscribed(id = podcast.id, subscribed = false)
        }
    }

    fun toggleAutoDownloadClicked() {
        val podcast = state.value.podcastWithEpisodes?.podcast
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
        val podcast = state.value.podcastWithEpisodes?.podcast
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
        val podcast = state.value.podcastWithEpisodes?.podcast
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
        val newSelections =
            selections.value.let {
                if (it.contains(episodeId)) {
                    it - episodeId
                } else {
                    it + episodeId
                }
            }
        selections.update { newSelections }
    }

    fun onSelectAllEpisodes() {
        val newSelections = state.value.podcastWithEpisodes?.episodes?.map { it.episode.id } ?: listOf()
        selections.update { newSelections }
    }

    fun onUnselectAllEpisodes() {
        selections.update { emptyList() }
    }

    fun onMarkSelectedAsPlayed() {
        val selected = state.value.podcastWithEpisodes?.episodes?.filter { it.selected } ?: return
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
        val selected = state.value.podcastWithEpisodes?.episodes?.filter { it.selected } ?: return
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
        val podcast = state.value.podcastWithEpisodes?.podcast
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
        val state = state.value
        if (!state.hasMorePages) {
            LogHelper.v(TAG, "Podcast next page requested but no more pages")
            return
        }
        val newPage = state.page + 1
        LogHelper.d(TAG, "Episodes next page requested: $newPage, Total: ${state.availableEpisodeCount}")
        page.update { newPage }
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
        val currentCount = state.value.availableEpisodeCount
        val episodesToLoadCount = currentCount + additionalCount
        longLivingScope.launch {
            loadingOlderEpisodes.update { true }
            episodesRepository.refreshForPodcastId(
                podcastId = podcastId,
                episodesToLoad = episodesToLoadCount,
                fetchSinceMostRecentEpisode = false,
            )
            loadingOlderEpisodes.update { false }
            onNextPageRequested()
        }
    }

    fun onSearchTermUpdated(searchTerm: String) {
        this.searchTerm.update { searchTerm }
    }

    companion object {
        private const val TAG = "PodcastDetails"
    }
}
