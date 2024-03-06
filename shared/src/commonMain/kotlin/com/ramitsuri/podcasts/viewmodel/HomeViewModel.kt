package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.HomeViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class HomeViewModel internal constructor(
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodesRepository: EpisodesRepository,
    private val clock: Clock,
) : ViewModel() {
    val state: StateFlow<HomeViewState> =
        podcastsAndEpisodesRepository.getSubscribedFlow()
            .map { HomeViewState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeViewState(listOf()))

    fun onEpisodePlayClicked(episodeId: String) {
    }

    fun onEpisodeAddToQueueClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.addToQueue(episodeId)
        }
    }

    fun onEpisodeRemoveFromQueueClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.removeFromQueue(episodeId)
        }
    }

    fun onEpisodeDownloadClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.download(episodeId)
        }
    }

    fun onEpisodeRemoveDownloadClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.removeDownload(episodeId)
        }
    }

    fun onEpisodeCancelDownloadClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.cancelDownload(episodeId)
        }
    }

    fun onEpisodePlayedClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.markPlayed(episodeId, clock.now())
        }
    }

    fun onEpisodeNotPlayedClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.markNotPlayed(episodeId)
        }
    }
}
