package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class SettingsViewModel internal constructor(
    private val settings: Settings,
    private val episodeFetcher: EpisodeFetcher,
    private val longLivingScope: CoroutineScope,
    private val clock: Clock,
) : ViewModel() {
    private val fetching = MutableStateFlow(false)

    private val versionClickTimes = mutableListOf<Instant>()

    val state =
        combine(
            settings.autoPlayNextInQueue(),
            settings.getLastEpisodeFetchTime(),
            settings.getRemoveCompletedEpisodesAfter(),
            settings.getRemoveUnfinishedEpisodesAfter(),
            fetching,
        ) { autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter, fetching ->
            SettingsViewState(
                autoPlayNextInQueue = autoPlayNextInQueue,
                lastFetchTime = lastFetchTime,
                removeCompletedAfter = removeCompletedAfter,
                removeUnfinishedAfter = removeUnfinishedAfter,
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

    fun onVersionClicked() {
        if (versionClickTimes.size == 6) {
            versionClickTimes.clear()
            viewModelScope.launch {
                settings.setShowLogQueueButton(settings.showLogQueueButton().first().not())
            }
            return
        }
        val clickTime = clock.now()
        val lastClickTime = versionClickTimes.lastOrNull() ?: clickTime
        if (clickTime - lastClickTime < 1.seconds) {
            versionClickTimes.add(clickTime)
        } else {
            versionClickTimes.clear()
            versionClickTimes.add(clickTime)
        }
    }
}
