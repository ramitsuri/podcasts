package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel internal constructor(
    private val settings: Settings,
    private val episodeFetcher: EpisodeFetcher,
    private val longLivingScope: CoroutineScope,
    private val clock: Clock,
) : ViewModel() {
    private val fetching = MutableStateFlow(false)

    val state =
        combine(
            settings.autoPlayNextInQueue(),
            settings.getLastEpisodeFetchTime(),
            settings.getRemoveCompletedEpisodesAfter(),
            settings.getRemoveUnfinishedEpisodesAfter(),
            fetching,
            settings.showYearEndReview(),
        ) { autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter, fetching,
            showYearEndReview,
            ->
            SettingsViewState(
                autoPlayNextInQueue = autoPlayNextInQueue,
                lastFetchTime = lastFetchTime,
                removeCompletedAfter = removeCompletedAfter,
                removeUnfinishedAfter = removeUnfinishedAfter,
                fetching = fetching,
                showYearEndReview = showYearEndReview,
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsViewState(),
        )

    fun toggleAutoPlayNextInQueue() {
        val currentAutoPlayNextInQueue = state.value.autoPlayNextInQueue
        longLivingScope.launch {
            settings.setAutoPlayNextInQueue(autoPlayNextInQueue = !currentAutoPlayNextInQueue)
        }
    }

    fun fetch() {
        longLivingScope.launch {
            fetching.update { true }
            episodeFetcher.fetchPodcastsIfNecessary(forced = true, downloaderTasksAllowed = true)
            fetching.update { false }
        }
    }

    fun setRemoveCompletedAfter(removeCompletedAfter: RemoveDownloadsAfter) {
        longLivingScope.launch {
            settings.setRemoveCompletedEpisodesAfter(removeCompletedAfter)
        }
    }

    fun setRemoveUnfinishedAfter(removeUnfinishedAfter: RemoveDownloadsAfter) {
        longLivingScope.launch {
            settings.setRemoveUnfinishedEpisodesAfter(removeUnfinishedAfter)
        }
    }

    private var lastTimeMs = clock.now()
    private var clickCount = 0

    fun onVersionClicked() {
        val now = clock.now()
        if (now.minus(lastTimeMs) < 500.milliseconds) {
            lastTimeMs = now
        } else {
            lastTimeMs = clock.now()
            clickCount = 0
        }
        clickCount++
        if (clickCount >= 7) {
            LogHelper.toggleRemoteLogging(enable = true)
            toggleShowYearEndReview()
        }
    }

    private fun toggleShowYearEndReview() {
        longLivingScope.launch {
            settings.setShowYearEndReview(!state.value.showYearEndReview)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T1, T2, T3, T4, T5, T6, R> combine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
    ): Flow<R> =
        kotlinx.coroutines.flow.combine(
            flow,
            flow2,
            flow3,
            flow4,
            flow5,
            flow6,
        ) { args: Array<*> ->
            transform(
                args[0] as T1,
                args[1] as T2,
                args[2] as T3,
                args[3] as T4,
                args[4] as T5,
                args[5] as T6,
            )
        }
}
