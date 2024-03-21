package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.HomeViewState
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel internal constructor(
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
    private val settings: Settings,
    private val longLivingScope: CoroutineScope,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                podcastsAndEpisodesRepository.getSubscribedFlow(),
                episodesRepository.getCurrentEpisode(),
                settings.getPlayingStateFlow(),
            ) { subscribedEpisodes, currentlyPlayingEpisode, playingState ->
                val currentlyPlaying =
                    if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                        currentlyPlayingEpisode
                    } else {
                        null
                    }
                Triple(subscribedEpisodes, currentlyPlaying, playingState)
            }.collect { (subscribedEpisodes, currentlyPlayingEpisode, playingState) ->
                _state.update {
                    it.copy(
                        episodes = subscribedEpisodes,
                        currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                        currentlyPlayingEpisodeState = playingState,
                    )
                }
            }
        }
    }

    fun onEpisodePlayClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.setCurrentlyPlayingEpisodeId(episode.id)
            playerController.play(episode)
        }
    }

    fun onEpisodePauseClicked() {
        playerController.pause()
    }

    fun onEpisodeAddToQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.addToQueue(episode.id)
        }
    }

    fun onEpisodeRemoveFromQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.removeFromQueue(episode.id)
        }
    }

    fun onEpisodeDownloadClicked(episodeId: String) {
        longLivingScope.launch {
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
            episodesRepository.markPlayed(episodeId)
        }
    }

    fun onEpisodeNotPlayedClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.markNotPlayed(episodeId)
        }
    }
}
