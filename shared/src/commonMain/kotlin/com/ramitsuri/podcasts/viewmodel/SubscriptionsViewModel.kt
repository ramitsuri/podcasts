package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.SubscriptionsViewState
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SubscriptionsViewModel(
    podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
) : ViewModel() {
    val state =
        podcastsAndEpisodesRepository
            .getSubscribedPodcastsFlow()
            .map { subscribedPodcasts ->
                SubscriptionsViewState(subscribedPodcasts = subscribedPodcasts.sortedBy { podcast -> podcast.title })
            }.stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SubscriptionsViewState(),
            )
}
