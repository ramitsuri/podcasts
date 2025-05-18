package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.TrendingPodcast

sealed interface ExploreViewState {
    data object Loading : ExploreViewState

    data class Success(val podcasts: List<TrendingPodcast>) : ExploreViewState
}
