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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val _page = MutableStateFlow(1L)
    private var availableEpisodeCount: Long = 0

    val state = _page
        .flatMapLatest { page ->
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
            }
        }
        .map { (subscribedPodcasts, subscribedEpisodes, currentlyPlayingEpisode, playingState) ->
            LogHelper.d(TAG, "Total episodes being shown: ${subscribedEpisodes.size}")
            HomeViewState(
                subscribedPodcasts = subscribedPodcasts,
                episodes = subscribedEpisodes,
                currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                currentlyPlayingEpisodeState = playingState,
            )
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeViewState(),
        )

    init {
        viewModelScope.launch {
            availableEpisodeCount = podcastsAndEpisodesRepository.getEpisodeCountForSubscribedPodcasts()
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
        if (state.episodes.size.toLong() == availableEpisodeCount) {
            LogHelper.v(TAG, "Episodes next page requested but no more episodes")
            return
        }
        val newPage = _page.value + 1
        LogHelper.d(TAG, "Episodes next page requested: $newPage")
        _page.update { newPage }
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
