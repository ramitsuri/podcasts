package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.SubscriptionsViewState
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionsViewModel(
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            podcastsAndEpisodesRepository.getSubscribedPodcastsFlow().collect { subscribedPodcasts ->
                _state.update {
                    it.copy(subscribedPodcasts = subscribedPodcasts.sortedBy { podcast -> podcast.title })
                }
            }
        }
    }
}
