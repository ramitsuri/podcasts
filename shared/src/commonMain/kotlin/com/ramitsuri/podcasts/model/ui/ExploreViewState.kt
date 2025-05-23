package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.TrendingPodcast

data class ExploreViewState(
    val isRefreshing: Boolean = false,
    val podcastsByCategory: Map<String, List<TrendingPodcast>> = mapOf(),
    val languages: List<String> = emptyList(),
    val selectedLanguage: String = "English",
    val categories: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val currentlyPlayingEpisodeArtworkUrl: String? = null,
)
