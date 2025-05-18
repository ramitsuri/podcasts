package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.QueueSort
import com.ramitsuri.podcasts.model.ui.QueueViewState
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.QueueRearrangementHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel internal constructor(
    episodeController: EpisodeController,
    settings: Settings,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
) : ViewModel(), EpisodeController by episodeController {
    private val queueRearrangementHelper =
        QueueRearrangementHelper(viewModelScope, episodesRepository, playerController)

    init {
        viewModelScope.launch {
            if (!settings.isDuplicateQueuePositionsIssueFixed()) {
                episodesRepository.getQueue().forEachIndexed { index, episode ->
                    episodesRepository.updateQueuePosition(episode.id, index)
                }
                settings.setIsDuplicateQueuePositionsIssueFixed(true)
            }
        }
    }

    val state =
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
            val episodes =
                subscribedEpisodes.map {
                    it.copy(queuePosition = queuePositions[it.id] ?: it.queuePosition)
                }.sortedBy { it.queuePosition }
            QueueViewState(
                episodes = episodes,
                currentlyPlayingEpisodeId = currentlyPlaying?.id,
                currentlyPlayingEpisodeState = playingState,
            )
        }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = QueueViewState(),
            )

    fun onEpisodeRearrangementRequested(
        position1: Int,
        position2: Int,
    ) {
        viewModelScope.launch {
            val currentlyAtPosition1 = state.value.episodes.getOrNull(position1)
            val currentlyAtPosition2 = state.value.episodes.getOrNull(position2)
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

    fun onEpisodesSortRequested(queueSort: QueueSort) {
        val episodes = state.value.episodes
        viewModelScope.launch {
            when (queueSort) {
                QueueSort.SHORTEST -> episodes.sortedBy { it.remainingDuration }
                QueueSort.LONGEST -> episodes.sortedByDescending { it.remainingDuration }
                QueueSort.OLDEST -> episodes.sortedBy { it.datePublished }
                QueueSort.NEWEST -> episodes.sortedByDescending { it.datePublished }
                QueueSort.PODCAST -> episodes.sortedWith(compareBy({ it.podcastName }, { it.datePublished }))
            }.forEachIndexed { index, episode ->
                episodesRepository.updateQueuePosition(episode.id, index)
            }
            playerController.updateQueue()
        }
    }
}
