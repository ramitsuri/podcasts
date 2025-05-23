package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.ExploreViewState
import com.ramitsuri.podcasts.repositories.TrendingPodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.CategoryHelper
import com.ramitsuri.podcasts.utils.LanguageHelper
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

class ExploreViewModel(
    private val trendingPodcastsRepository: TrendingPodcastsRepository,
    private val settings: Settings,
    private val clock: Clock,
    private val languageHelper: LanguageHelper,
    private val categoryHelper: CategoryHelper,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)

    val state =
        combine(
            trendingPodcastsRepository.getAllFlow(),
            settings.getTrendingPodcastsLanguage(),
            settings.getTrendingPodcastsCategories(categoryHelper.defaultCategories),
            isRefreshing,
        ) { podcasts, selectedLanguage, selectedCategories, isRefreshing ->
            val podcastsByCategory =
                selectedCategories
                    .sorted()
                    .map { category ->
                        val podcastsWithCategory =
                            podcasts
                                .filter { podcast -> podcast.categories.map { it.name }.contains(category) }
                        category to podcastsWithCategory
                    }.associate { it }
            ExploreViewState(
                isRefreshing = isRefreshing,
                podcastsByCategory = podcastsByCategory,
                languages = languageHelper.getAvailableLanguages(),
                selectedLanguage = selectedLanguage,
                categories = categoryHelper.categories,
                selectedCategories = selectedCategories.sorted(),
            )
        }.onStart {
            refreshIfNecessary()
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExploreViewState(),
        )

    fun onLanguageClicked(language: String) {
        LogHelper.d(TAG, "onLanguageClicked: $language")
        viewModelScope.launch {
            settings.setTrendingPodcastsLanguage(language)
        }
    }

    fun onCategoryClicked(category: String) {
        LogHelper.d(TAG, "onCategoryClicked: $category")
        viewModelScope.launch {
            val currentCategories = state.value.selectedCategories
            val newCategories =
                if (category in currentCategories) {
                    currentCategories - category
                } else {
                    if (currentCategories.size == MAX_CATEGORIES) {
                        return@launch
                    }
                    currentCategories + category
                }
            LogHelper.d(TAG, "New categories: $newCategories")
            settings.setTrendingPodcastsCategories(newCategories)
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            refreshIfNecessary(forced = true)
        }
    }

    private suspend fun refreshIfNecessary(forced: Boolean = false) {
        isRefreshing.value = true
        if (hasCacheExpired() || forced) {
            val language = state.value.selectedLanguage
            val languages = languageHelper.getLanguageCodesForLanguage(language)
            val categories = state.value.selectedCategories
            trendingPodcastsRepository
                .refresh(languages = languages, categories = categories)
                .let { successful ->
                    if (successful) {
                        settings.setLastTrendingPodcastsFetchTime()
                    }
                }
        } else {
            LogHelper.d(TAG, "Trending podcasts cache is still valid or not forced, won't refresh")
        }
        isRefreshing.value = false
    }

    private suspend fun hasCacheExpired(): Boolean {
        return clock.now().minus(
            settings.getLastTrendingPodcastsFetchTime(),
        ) >= CACHE_EXPIRATION_IN_HOURS.hours
    }

    companion object {
        private const val TAG = "ExploreViewModel"
        private const val CACHE_EXPIRATION_IN_HOURS = 3 * 24 // 3 days
        private const val MAX_CATEGORIES = 5
    }
}
