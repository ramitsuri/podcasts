package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EpisodeDetailsViewModel internal constructor(
    episodeId: String?,
    repository: EpisodesRepository,
) : ViewModel() {
    val state: StateFlow<EpisodeDetailsViewState> =
        if (episodeId == null) {
            MutableStateFlow(EpisodeDetailsViewState())
        } else {
            repository.getEpisodeFlow(episodeId)
                // TODO Provide playing state
                .map { EpisodeDetailsViewState(it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EpisodeDetailsViewState())
        }

    companion object
}
