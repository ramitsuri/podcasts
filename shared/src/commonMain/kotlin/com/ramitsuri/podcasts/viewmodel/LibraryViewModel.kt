package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.LibraryViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel internal constructor(
    episodesRepository: EpisodesRepository,
    settings: Settings,
) : ViewModel() {
    val state =
        combine(
            episodesRepository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { currentlyPlayingEpisode, playingState ->
            val currentlyPlaying =
                if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                    currentlyPlayingEpisode
                } else {
                    null
                }
            LibraryViewState(
                currentlyPlayingEpisodeArtworkUrl = currentlyPlaying?.podcastImageUrl,
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryViewState(),
        )
}
