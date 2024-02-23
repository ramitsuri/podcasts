package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.HomeViewState
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel internal constructor(
    private val repository: PodcastsAndEpisodesRepository,
) : ViewModel() {
    val state: StateFlow<HomeViewState> =
        repository.getSubscribed()
            .map { HomeViewState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeViewState(listOf()))
}
