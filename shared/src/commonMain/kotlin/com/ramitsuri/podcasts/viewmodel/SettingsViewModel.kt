package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel internal constructor(
    private val settings: Settings,
    private val episodeFetcher: EpisodeFetcher,
    private val longLivingScope: CoroutineScope,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.autoPlayNextInQueue().collect { autoPlayNextInQueue ->
                _state.update {
                    it.copy(autoPlayNextInQueue = autoPlayNextInQueue)
                }
            }
        }
    }

    fun toggleAutoPlayNextInQueue() {
        val currentAutoPlayNextInQueue = _state.value.autoPlayNextInQueue
        viewModelScope.launch {
            settings.setAutoPlayNextInQueue(autoPlayNextInQueue = !currentAutoPlayNextInQueue)
        }
    }

    fun fetch() {
        longLivingScope.launch {
            _state.update { it.copy(fetching = true) }
            episodeFetcher.fetchPodcastsIfNecessary(forced = true)
            val fetchTime = settings.getLastEpisodeFetchTime()
            _state.update { it.copy(lastFetchTime = fetchTime, fetching = false) }
        }
    }
}
