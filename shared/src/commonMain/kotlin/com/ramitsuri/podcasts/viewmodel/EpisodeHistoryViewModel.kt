package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.SessionEpisode
import com.ramitsuri.podcasts.model.ui.EpisodeHistory
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
            // getEpisodesUpdated is being observed so that updates to Episodes table can trigger updates here.
            episodesRepository.getEpisodesUpdated(),
            repository.getEpisodeHistory(timeZone),
            episodesRepository.getCurrentEpisode(),
            settings.getPlayingStateFlow(),
        ) { _, episodeHistories, currentlyPlayingEpisode, playingState ->
            val currentlyPlaying =
                if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                    currentlyPlayingEpisode
                } else {
                    null
                }
            val episodes = episodesRepository.getEpisodes(episodeHistories.map { it.episodeId }.distinct())
            EpisodeHistoryViewState(episodeHistories.groupedByDate(episodes), currentlyPlaying?.id, playingState)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EpisodeHistoryViewState(),
        )

    private fun List<SessionEpisode>.groupedByDate(episodes: List<Episode>): Map<LocalDate, List<EpisodeHistory>> {
        return groupBy { sessionEpisode ->
            sessionEpisode.time.toLocalDateTime(timeZone).date
        }.mapValues { (_, sessionEpisodes) ->
            sessionEpisodes
                .sortedByDescending { it.time }
                .mapNotNull { sessionEpisode ->
                    episodes
                        .firstOrNull { it.id == sessionEpisode.episodeId }
                        ?.let { episode ->
                            EpisodeHistory(
                                episode,
                                sessionEpisode.sessionId,
                                sessionEpisode.time,
                            )
                        }
                }
        }
    }
}
