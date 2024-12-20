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
    val totalActualDurationListened: Duration, // This is if speed was not 1x
    val totalEpisodesListened: Int,
    val mostListenedOnDayOfWeek: DayOfWeek,
    val mostListenedOnDay: LocalDate,
    val mostListenedMonth: Month,
)
