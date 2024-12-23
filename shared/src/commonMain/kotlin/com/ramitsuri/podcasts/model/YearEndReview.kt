package com.ramitsuri.podcasts.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.time.Duration

data class YearEndReview(
    val listeningSince: LocalDateTime,
    val mostListenedToPodcasts: List<Long>,
    val totalDurationListened: Duration,
    // This is if speed was not 1x
    val totalConsumedDuration: Duration,
    val totalEpisodesListened: Int,
    val mostListenedOnDayOfWeek: MostListenedDayOfWeek,
    val mostListenedDate: MostListenedDate,
    val mostListenedMonth: MostListenedMonth,
) {
    data class MostListenedDayOfWeek(
        val dayOfWeek: DayOfWeek,
        val duration: Duration,
    )

    data class MostListenedMonth(
        val month: Month,
        val duration: Duration,
    )

    data class MostListenedDate(
        val date: LocalDate,
        val duration: Duration,
    )
}
