package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.HomeViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class HomeViewModel internal constructor(
    private val podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val episodeController: EpisodeController,
    private val episodesRepository: EpisodesRepository,
    private val settings: Settings,
    private val podcastsRepository: PodcastsRepository,
    private val sessionHistoryRepository: SessionHistoryRepository,
    private val clock: Clock,
    private val episodeFetcher: EpisodeFetcher,
) : ViewModel(), EpisodeController by episodeController {
    private val page = MutableStateFlow(1L)
    private var availableEpisodeCount: Long = 0
    private val isRefreshing = MutableStateFlow(false)

    val state =
        page
            .flatMapLatest { page ->
                combine(
                    podcastsAndEpisodesRepository.getSubscribedPodcastsFlow(),
                    podcastsAndEpisodesRepository.getSubscribedFlow(page),
                    episodesRepository.getCurrentEpisode(),
                    settings.getPlayingStateFlow(),
                    sessionHistoryRepository.hasSessions(),
                    isRefreshing,
                    settings.hasSeenWidgetItem(),
                ) { subscribedPodcasts, subscribedEpisodes, currentlyPlayingEpisode, playingState, hasSessions,
                    isRefreshing, hasSeenWidgetItem,
                    ->
                    LogHelper.d(TAG, "Total episodes being shown: ${subscribedEpisodes.size}")
                    val currentlyPlaying =
                        if (playingState == PlayingState.PLAYING || playingState == PlayingState.LOADING) {
                            currentlyPlayingEpisode
                        } else {
                            null
                        }
                    HomeViewState(
                        subscribedPodcasts = subscribedPodcasts.take(10),
                        episodes = subscribedEpisodes,
                        currentlyPlayingEpisodeId = currentlyPlaying?.id,
                        currentlyPlayingEpisodeState = playingState,
                        currentlyPlayingEpisodeArtworkUrl = currentlyPlaying?.podcastImageUrl,
                        showYearEndReview = hasSessions && clock.now() < Instant.parse(SHOW_REVIEW_UNTIL),
                        isRefreshing = isRefreshing,
                        showSettingsBadge = !hasSeenWidgetItem,
                    )
                }
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeViewState(),
            )

    init {
        viewModelScope.launch {
            availableEpisodeCount = podcastsAndEpisodesRepository.getEpisodeCountForSubscribedPodcasts()
        }
    }

    fun markPodcastHasNewSeen(podcastId: Long): Boolean {
        val hadNewEpisodes = state.value.subscribedPodcasts.firstOrNull { it.id == podcastId }?.hasNewEpisodes == true
        viewModelScope.launch {
            podcastsRepository.updateHasNewEpisodes(podcastId, false)
        }
        return hadNewEpisodes
    }

    fun onNextPageRequested() {
        if (state.value.episodes.size.toLong() == availableEpisodeCount) {
            LogHelper.d(TAG, "Episodes next page requested but no more episodes")
            return
        }
        val newPage = page.value + 1
        LogHelper.d(TAG, "Episodes next page requested: $newPage")
        page.update { newPage }
    }

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.update { true }
            episodeFetcher.fetchPodcastsIfNecessary(forced = true, downloaderTasksAllowed = true)
            isRefreshing.update { false }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val SHOW_REVIEW_UNTIL = "2026-01-10T12:00:00Z"
    }
}
