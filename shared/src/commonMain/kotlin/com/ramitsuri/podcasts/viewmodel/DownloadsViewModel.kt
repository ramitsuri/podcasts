package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.DownloadsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadsViewModel internal constructor(
    episodeController: EpisodeController,
    settings: Settings,
    private val episodesRepository: EpisodesRepository,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(DownloadsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                episodesRepository.getDownloadedFlow(),
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
}
