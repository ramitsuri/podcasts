package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class EpisodeDetailsViewModel internal constructor(
    episodeId: String?,
    repository: EpisodesRepository,
    episodeController: EpisodeController,
    settings: Settings,
) : ViewModel(), EpisodeController by episodeController {
    val state =
        combine(
            repository.getEpisodeFlow(episodeId ?: ""),
            repository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { episode, currentlyPlayingEpisode, playingState ->
            val currentlyPlaying =
                if (episode != null && episode.id == currentlyPlayingEpisode?.id) {
                    playingState
                } else {
                    PlayingState.NOT_PLAYING
                }
            EpisodeDetailsViewState(episode, currentlyPlaying)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EpisodeDetailsViewState(),
        )

    init {
        if (episodeId == null) {
            LogHelper.v(TAG, "Episode id is null")
        }
    }

    companion object {
        private const val TAG = "EpisodeDetails"
    }
}
