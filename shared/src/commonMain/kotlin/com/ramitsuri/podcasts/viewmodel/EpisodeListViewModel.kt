package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeListType
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeListViewState
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EpisodeListViewModel internal constructor(
    episodeListType: EpisodeListType,
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
    private val episodeDownloader: EpisodeDownloader,
    private val settings: Settings,
    private val longLivingScope: CoroutineScope,
) : ViewModel() {
    private val _state = MutableStateFlow(EpisodeListViewState())
    val state = _state.asStateFlow()

    init {
        val episodeList = when (episodeListType) {
            EpisodeListType.SUBSCRIBED -> podcastsAndEpisodesRepository.getSubscribedFlow()
            EpisodeListType.QUEUE -> episodesRepository.getQueueFlow()
        }
        viewModelScope.launch {
            combine(
                episodeList,
                episodesRepository.getCurrentEpisode(),
                settings.getPlayingStateFlow(),
            ) { subscribedEpisodes, currentlyPlayingEpisode, playingState ->
                val currentlyPlaying =
                    if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                        currentlyPlayingEpisode
                    } else {
                        null
                    }
                Triple(subscribedEpisodes, currentlyPlaying, playingState)
            }.collect { (subscribedEpisodes, currentlyPlayingEpisode, playingState) ->
                _state.update {
                    it.copy(
                        episodes = subscribedEpisodes,
                        currentlyPlayingEpisodeId = currentlyPlayingEpisode?.id,
                        currentlyPlayingEpisodeState = playingState,
                    )
                }
            }
        }
    }

    fun onEpisodePlayClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.setCurrentlyPlayingEpisodeId(episode.id)
            playerController.play(episode)
        }
    }

    fun onEpisodePauseClicked() {
        playerController.pause()
    }

    fun onEpisodeAddToQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.addToQueue(episode.id)
        }
    }

    fun onEpisodeRemoveFromQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.removeFromQueue(episode.id)
        }
    }

    fun onEpisodeDownloadClicked(episode: Episode) {
        episodeDownloader.add(episode)
    }

    fun onEpisodeRemoveDownloadClicked(episode: Episode) {
        episodeDownloader.remove(episode)
    }

    fun onEpisodeCancelDownloadClicked(episode: Episode) {
        episodeDownloader.cancel(episode)
    }

    fun onEpisodePlayedClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.markPlayed(episodeId)
        }
    }

    fun onEpisodeNotPlayedClicked(episodeId: String) {
        viewModelScope.launch {
            episodesRepository.markNotPlayed(episodeId)
        }
    }

    fun test(from: Int, to: Int) {
        viewModelScope.launch {
            val currentlyAtFrom = _state.value.episodes.getOrNull(from)
            val currentlyAtTo = _state.value.episodes.getOrNull(to)
            if (currentlyAtFrom != null && currentlyAtTo != null) {
                episodesRepository.updateQueuePositions(
                    mapOf(
                        currentlyAtFrom.id to to,
                        currentlyAtTo.id to from,
                    ),
                )
            }
        }
    }
}
