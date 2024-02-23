package com.ramitsuri.podcasts.viewmodel

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope

    protected open fun onCleared()
}
