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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel internal constructor(
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    episodeController: EpisodeController,
    episodesRepository: EpisodesRepository,
    settings: Settings,
    private val podcastsRepository: PodcastsRepository,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                podcastsAndEpisodesRepository.getSubscribedPodcastsFlow(),
                podcastsAndEpisodesRepository.getSubscribedFlow(),
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

    fun markPodcastHasNewSeen(podcastId: Long): Boolean {
        val hadNewEpisodes = _state.value.subscribedPodcasts.firstOrNull { it.id == podcastId }?.hasNewEpisodes == true
        viewModelScope.launch {
            podcastsRepository.updateHasNewEpisodes(podcastId, false)
        }
        return hadNewEpisodes
    }

    private data class Data(
        val podcasts: List<Podcast>,
        val episodes: List<Episode>,
        val currentEpisode: Episode?,
        val playingState: PlayingState,
    )
}
