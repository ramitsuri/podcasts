package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.ui.YearEndReviewViewState
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class YearEndReviewViewModel internal constructor(
    podcastsRepository: PodcastsRepository,
    sessionHistoryRepository: SessionHistoryRepository,
    timeZone: TimeZone,
) : ViewModel() {
    private val _state: MutableStateFlow<YearEndReviewViewState> = MutableStateFlow(YearEndReviewViewState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val review = sessionHistoryRepository.getReview(YEAR, timeZone)

            val mostListenedToPodcasts =
                review.mostListenedToPodcasts.mapNotNull { podcastId ->
                    podcastsRepository.get(podcastId)
                }
            _state.update {
                if (mostListenedToPodcasts.isEmpty()) {
                    LogHelper.v(TAG, "No podcasts found for mostListenedToPodcasts")
                    YearEndReviewViewState.Error
                } else {
                    YearEndReviewViewState.Data(
                        year = YEAR,
                        listeningSince = review.listeningSince,
                        mostListenedToPodcasts = mostListenedToPodcasts,
                        totalDurationListened = review.totalDurationListened,
                        totalActualDurationListened = review.totalActualDurationListened,
                        totalEpisodesListened = review.totalEpisodesListened,
                        mostListenedOnDayOfWeek = review.mostListenedOnDayOfWeek,
                        mostListenedOnDay = review.mostListenedOnDay,
                        mostListenedMonth = review.mostListenedMonth,
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "YearEndReviewViewModel"
        private const val YEAR = 2024
    }
}
