package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EpisodeDetailsViewModel internal constructor(
    episodeId: String?,
    repository: EpisodesRepository,
    episodeController: EpisodeController,
    settings: Settings,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(EpisodeDetailsViewState())
    val state = _state.asStateFlow()

    init {
        if (episodeId != null) {
            viewModelScope.launch {
                combine(
                    repository.getEpisodeFlow(episodeId),
                    repository.getCurrentEpisode(),
                    settings.getPlayingStateFlow(),
                ) { episode, currentlyPlayingEpisode, playingState ->
                    val currentlyPlaying = if (episode != null && episode.id == currentlyPlayingEpisode?.id) {
                        playingState
                    } else {
                        PlayingState.NOT_PLAYING
                    }
                    Pair(episode, currentlyPlaying)
                }.collect { (episode, playingState) ->
                    _state.update {
                        it.copy(
                            episode = episode,
                            playingState = playingState,
                        )
                    }
                }
            }
        } else {
            LogHelper.v(TAG, "Episode id is null")
        }
    }
    companion object {
        private const val TAG = "EpisodeDetails"
    }
}
