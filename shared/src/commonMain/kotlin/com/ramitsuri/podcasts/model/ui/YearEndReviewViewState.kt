package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Podcast
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.time.Duration

sealed interface YearEndReviewViewState {
    data object Loading : YearEndReviewViewState

    data object Error : YearEndReviewViewState

    data class Data(
        val year: Int,
        val listeningSince: LocalDateTime,
        val mostListenedToPodcasts: List<Podcast>,
        val totalDurationListened: Duration,
        val totalActualDurationListened: Duration,
        val totalEpisodesListened: Int,
        val mostListenedOnDayOfWeek: DayOfWeek,
        val mostListenedOnDay: LocalDate,
        val mostListenedMonth: Month,
    ) : YearEndReviewViewState
}
