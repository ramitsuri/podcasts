package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.DownloadsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DownloadsViewModel internal constructor(
    episodeController: EpisodeController,
    settings: Settings,
    episodesRepository: EpisodesRepository,
) : ViewModel(), EpisodeController by episodeController {
    val state =
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
            DownloadsViewState(subscribedEpisodes, currentlyPlaying?.id, playingState)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadsViewState(),
        )
}
