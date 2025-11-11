package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.RemoteConfigHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class EpisodeDetailsViewModel internal constructor(
    episodeId: String,
    podcastId: Long,
    repository: EpisodesRepository,
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    episodeController: EpisodeController,
    settings: Settings,
    remoteConfigHelper: RemoteConfigHelper,
) : ViewModel(), EpisodeController by episodeController {
    val state =
        combine(
            podcastsAndEpisodesRepository.getEpisodeFlow(podcastId, episodeId),
            repository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { episode, currentlyPlayingEpisode, playingState ->
            val currentlyPlaying =
                if (episode != null && episode.id == currentlyPlayingEpisode?.id) {
                    playingState
                } else {
                    PlayingState.NOT_PLAYING
                }
            EpisodeDetailsViewState(
                loading = false,
                episode = episode,
                playingState = currentlyPlaying,
                allowSharingToNotificationJournal = remoteConfigHelper.isDevicePrivileged(),
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EpisodeDetailsViewState(),
        )

    companion object {
        private const val TAG = "EpisodeDetails"
    }
}
