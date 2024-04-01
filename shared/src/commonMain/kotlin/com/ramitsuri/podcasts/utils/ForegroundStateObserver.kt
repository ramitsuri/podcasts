package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.model.ForegroundState
import kotlinx.coroutines.flow.StateFlow

interface ForegroundStateObserver {
    val state: StateFlow<ForegroundState>
}
