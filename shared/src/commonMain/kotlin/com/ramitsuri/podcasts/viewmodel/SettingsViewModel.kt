package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
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
            settings.shouldDownloadOnWifiOnly(),
            settings.hasSeenWidgetItem(),
            fetching,
        ) { autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter, shouldDownloadOnWifiOnly,
            hasSeenWidgetItem, fetching,
            ->
            SettingsViewState(
                autoPlayNextInQueue = autoPlayNextInQueue,
                lastFetchTime = lastFetchTime,
                removeCompletedAfter = removeCompletedAfter,
                removeUnfinishedAfter = removeUnfinishedAfter,
                shouldDownloadOnWifiOnly = shouldDownloadOnWifiOnly,
                showWidgetNewBadge = !hasSeenWidgetItem,
                fetching = fetching,
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

    fun toggleShouldDownloadOnWifiOnly() {
        val currentShouldDownloadOnWifiOnly = state.value.shouldDownloadOnWifiOnly
        longLivingScope.launch {
            settings.setShouldDownloadOnWifiOnly(shouldDownloadOnWifiOnly = !currentShouldDownloadOnWifiOnly)
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

    fun onWidgetItemSeen() {
        viewModelScope.launch {
            settings.setHasSeenWidgetItem()
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
            longLivingScope.launch {
                val remoteLoggingEnabled = settings.isRemoteLoggingEnabled().first()
                settings.setIsRemoteLoggingEnabled(!remoteLoggingEnabled)
                LogHelper.toggleRemoteLogging(!remoteLoggingEnabled)
            }
        }
    }
}
