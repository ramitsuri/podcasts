package com.ramitsuri.podcasts.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ramitsuri.podcasts.model.ForegroundState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class AndroidForegroundStateObserver : ForegroundStateObserver, LifecycleEventObserver {
    private val _state = MutableStateFlow(ForegroundState(isInForeground = false))
    override val state: StateFlow<ForegroundState>
        get() = _state.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            _state.update { ForegroundState(isInForeground = true) }
        }
        if (event == Lifecycle.Event.ON_STOP) {
            _state.update { ForegroundState(isInForeground = false) }
        }
    }

}
