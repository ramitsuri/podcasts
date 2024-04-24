package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class SettingsViewModel internal constructor(
    private val settings: Settings,
    private val episodeFetcher: EpisodeFetcher,
    private val longLivingScope: CoroutineScope,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settings.autoPlayNextInQueue(),
                settings.getLastEpisodeFetchTime(),
                settings.getRemoveCompletedEpisodesAfter(),
                settings.getRemoveUnfinishedEpisodesAfter(),
            ) { autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter ->
                Data(autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter)
            }.collect { (autoPlayNextInQueue, lastFetchTime, removeCompletedAfter, removeUnfinishedAfter) ->
                _state.update {
                    it.copy(
                        autoPlayNextInQueue = autoPlayNextInQueue,
                        lastFetchTime = lastFetchTime,
                        removeCompletedAfter = removeCompletedAfter,
                        removeUnfinishedAfter = removeUnfinishedAfter,
                    )
                }
            }
        }
    }

    fun toggleAutoPlayNextInQueue() {
        val currentAutoPlayNextInQueue = _state.value.autoPlayNextInQueue
        longLivingScope.launch {
            settings.setAutoPlayNextInQueue(autoPlayNextInQueue = !currentAutoPlayNextInQueue)
        }
    }

    fun fetch() {
        longLivingScope.launch {
            _state.update { it.copy(fetching = true) }
            episodeFetcher.fetchPodcastsIfNecessary(forced = true, downloaderTasksAllowed = true)
            _state.update { it.copy(fetching = false) }
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

    private data class Data(
        val autoPlayNextInQueue: Boolean,
        val lastFetchTime: Instant,
        val removeCompletedAfter: RemoveDownloadsAfter,
        val removeUnfinishedAfter: RemoveDownloadsAfter,
    )
}
