package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.HomeViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel internal constructor(
    private val podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodeController: EpisodeController,
    private val episodesRepository: EpisodesRepository,
    private val settings: Settings,
    private val podcastsRepository: PodcastsRepository,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    private var updatePodcastsAndEpisodesJob: Job? = null

    init {
        viewModelScope.launch {
            launch {
                _state.update {
                    val episodeCount = podcastsAndEpisodesRepository.getEpisodeCountForSubscribedPodcasts()
                    it.copy(availableEpisodeCount = episodeCount)
                }
            }
            updatePodcastsAndEpisodes()
        }
    }

    fun markPodcastHasNewSeen(podcastId: Long): Boolean {
        val hadNewEpisodes = _state.value.subscribedPodcasts.firstOrNull { it.id == podcastId }?.hasNewEpisodes == true
        viewModelScope.launch {
            podcastsRepository.updateHasNewEpisodes(podcastId, false)
        }
        return hadNewEpisodes
    }

    fun onNextPageRequested() {
        val state = _state.value
        val availableEpisodeCount = state.availableEpisodeCount
        if (state.episodes.size.toLong() == availableEpisodeCount) {
            LogHelper.v(TAG, "Episodes next page requested but no more episodes")
            return
        }
        val newPage = state.page + 1
        LogHelper.d(TAG, "Episodes next page requested: $newPage")
        _state.update { it.copy(page = newPage) }
        updatePodcastsAndEpisodes()
    }

    private fun updatePodcastsAndEpisodes() {
        val page = _state.value.page
        updatePodcastsAndEpisodesJob?.cancel()
        updatePodcastsAndEpisodesJob = viewModelScope.launch {
            combine(
                podcastsAndEpisodesRepository.getSubscribedPodcastsFlow(),
                podcastsAndEpisodesRepository.getSubscribedFlow(page),
                episodesRepository.getCurrentEpisode(),
                settings.getPlayingStateFlow(),
            ) { subscribedPodcasts, subscribedEpisodes, currentlyPlayingEpisode, playingState ->
                val currentlyPlaying =
                    if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                        currentlyPlayingEpisode
                    } else {
                        null
                    }
                Data(subscribedPodcasts, subscribedEpisodes, currentlyPlaying, playingState)
            }.collect { (subscribedPodcasts, subscribedEpisodes, currentlyPlayingEpisode, playingState) ->
                LogHelper.d(TAG, "Total episodes being shown: ${subscribedEpisodes.size}")
                _state.update {
                    it.copy(
                        subscribedPodcasts = subscribedPodcasts,
                        episodes = subscribedEpisodes,
                        currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                        currentlyPlayingEpisodeState = playingState,
                    )
                }
            }
        }
    }

    private data class Data(
        val podcasts: List<Podcast>,
        val episodes: List<Episode>,
        val currentEpisode: Episode?,
        val playingState: PlayingState,
    )

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
