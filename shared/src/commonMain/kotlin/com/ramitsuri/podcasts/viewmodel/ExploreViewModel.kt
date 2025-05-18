package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.ExploreViewState
import com.ramitsuri.podcasts.repositories.TrendingPodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

class ExploreViewModel(
    private val trendingPodcastsRepository: TrendingPodcastsRepository,
    private val settings: Settings,
    private val clock: Clock,
) : ViewModel() {
    private var refreshDone = false

    val state =
        trendingPodcastsRepository
            .getAllFlow()
            .map {
                if (refreshDone) {
                    ExploreViewState.Success(it)
                } else {
                    ExploreViewState.Loading
                }
            }.onStart {
                refreshIfNecessary()
            }.stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ExploreViewState.Loading,
            )

    private suspend fun refreshIfNecessary() {
        if (clock.now().minus(
                settings.getLastTrendingPodcastsFetchTime(),
            ) >= CACHE_EXPIRATION_IN_HOURS.hours
        ) {
            trendingPodcastsRepository.refresh().let { successful ->
                if (successful) {
                    settings.setLastTrendingPodcastsFetchTime()
                }
            }
        } else {
            LogHelper.d(TAG, "Refreshed trending podcasts recently, won't refresh again")
        }
        refreshDone = true
    }

    companion object {
        private const val CACHE_EXPIRATION_IN_HOURS = 3 * 24 // 3 days
        private const val TAG = "ExploreViewModel"
    }
}
