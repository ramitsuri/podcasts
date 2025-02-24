package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class EpisodeDetailsViewModel internal constructor(
    episodeId: String?,
    podcastId: Long?,
    repository: EpisodesRepository,
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    episodeController: EpisodeController,
    settings: Settings,
) : ViewModel(), EpisodeController by episodeController {
    private var alreadyAttemptedToLoadMissing: Boolean = false
    val state =
        combine(
            repository.getEpisodeFlow(episodeId ?: ""),
            repository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { episode, currentlyPlayingEpisode, playingState ->
            if (episode == null) {
                if (alreadyAttemptedToLoadMissing) {
                    // do nothing
                } else if (podcastId != null && episodeId != null) {
                    podcastsAndEpisodesRepository.loadMissingEpisode(podcastId = podcastId, episodeId = episodeId)
                    alreadyAttemptedToLoadMissing = true
                } else {
                    alreadyAttemptedToLoadMissing = true
                }
            }
            val currentlyPlaying =
                if (episode != null && episode.id == currentlyPlayingEpisode?.id) {
                    playingState
                } else {
                    PlayingState.NOT_PLAYING
                }
            EpisodeDetailsViewState(
                loading = !alreadyAttemptedToLoadMissing,
                episode = episode,
                playingState = currentlyPlaying,
            )
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
