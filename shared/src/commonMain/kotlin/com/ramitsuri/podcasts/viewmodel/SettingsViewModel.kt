package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel internal constructor(
    private val settings: Settings,
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
}
