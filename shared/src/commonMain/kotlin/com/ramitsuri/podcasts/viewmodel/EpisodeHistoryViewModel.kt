package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeHistory
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeHistoryViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val _state = MutableStateFlow(EpisodeHistoryViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getEpisodeHistory(),
                episodesRepository.getCurrentEpisode(),
                settings.getPlayingStateFlow(),
            ) { episodeHistories, currentlyPlayingEpisode, playingState ->
                val currentlyPlaying =
                    if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                        currentlyPlayingEpisode
                    } else {
                        null
                    }
                Triple(episodeHistories, currentlyPlaying, playingState)
            }.collect { (episodeHistories, currentlyPlayingEpisode, playingState) ->
                _state.update {
                    it.copy(
                        episodesByDate = episodeHistories.groupedByDate(),
                        currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                        currentlyPlayingEpisodeState = playingState,
                    )
                }
            }
        }
    }

    private fun List<EpisodeHistory>.groupedByDate(): Map<LocalDate, List<Episode>> {
        return groupBy { episodeHistory ->
            episodeHistory.time.toLocalDateTime(timeZone).date
        }.mapValues { (_, episodeHistories) ->
            episodeHistories
                .sortedByDescending { it.time }
                .map { it.episode }
        }
    }
}
