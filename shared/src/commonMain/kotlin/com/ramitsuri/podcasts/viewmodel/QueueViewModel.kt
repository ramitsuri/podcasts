package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.QueueViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.QueueRearrangementHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QueueViewModel internal constructor(
    episodeController: EpisodeController,
    settings: Settings,
    private val episodesRepository: EpisodesRepository,
) : ViewModel(), EpisodeController by episodeController {
    private val _state = MutableStateFlow(QueueViewState())
    val state = _state.asStateFlow()

    private val queueRearrangementHelper = QueueRearrangementHelper(viewModelScope, episodesRepository)

    init {
        viewModelScope.launch {
            combine(
                queueRearrangementHelper.queuePositions,
                episodesRepository.getQueueFlow(),
                episodesRepository.getCurrentEpisode(),
                settings.getPlayingStateFlow(),
            ) { queuePositions, subscribedEpisodes, currentlyPlayingEpisode, playingState ->
                val currentlyPlaying =
                    if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                        currentlyPlayingEpisode
                    } else {
                        null
                    }
                Data(queuePositions, subscribedEpisodes, currentlyPlaying, playingState)
            }.collect { (queuePositions, subscribedEpisodes, currentlyPlayingEpisode, playingState) ->
                val episodes = subscribedEpisodes.map {
                    it.copy(queuePosition = queuePositions[it.id] ?: it.queuePosition)
                }.sortedBy { it.queuePosition }
                _state.update { viewState ->
                    viewState.copy(
                        episodes = episodes,
                        currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                        currentlyPlayingEpisodeState = playingState,
                    )
                }
            }
        }
    }

    fun onEpisodeRearrangementRequested(
        position1: Int,
        position2: Int,
    ) {
        viewModelScope.launch {
            val currentlyAtPosition1 = _state.value.episodes.getOrNull(position1)
            val currentlyAtPosition2 = _state.value.episodes.getOrNull(position2)
            if (currentlyAtPosition1 != null && currentlyAtPosition2 != null) {
                queueRearrangementHelper.updateQueuePositions(
                    currentlyAtPosition1.id,
                    currentlyAtPosition2.queuePosition,
                    currentlyAtPosition2.id,
                    currentlyAtPosition1.queuePosition,
                )
            }
        }
    }

    private data class Data(
        val queuePosition: Map<String, Int>,
        val subscribedEpisodes: List<Episode>,
        val currentlyPlaying: Episode?,
        val playingState: PlayingState
    )
}
