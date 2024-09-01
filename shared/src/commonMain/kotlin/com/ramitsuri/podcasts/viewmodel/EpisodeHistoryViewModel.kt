package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeHistory
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeHistoryViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EpisodeHistoryViewModel internal constructor(
    episodeController: EpisodeController,
    episodesRepository: EpisodesRepository,
    repository: SessionHistoryRepository,
    settings: Settings,
    private val timeZone: TimeZone,
) : ViewModel(), EpisodeController by episodeController {
    val state =
        combine(
            repository.getEpisodeHistory(timeZone),
            episodesRepository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { episodeHistories, currentlyPlayingEpisode, playingState ->
            val currentlyPlaying =
                if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                    currentlyPlayingEpisode
                } else {
                    null
                }
            EpisodeHistoryViewState(episodeHistories.groupedByDate(), currentlyPlaying?.id, playingState)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EpisodeHistoryViewState(),
        )

    private fun List<EpisodeHistory>.groupedByDate(): Map<LocalDate, List<EpisodeHistory>> {
        return groupBy { episodeHistory ->
            episodeHistory.time.toLocalDateTime(timeZone).date
        }.mapValues { (_, episodeHistories) ->
            episodeHistories
                .sortedByDescending { it.time }
        }
    }
}
