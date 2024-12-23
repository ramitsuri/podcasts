package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.YearEndReview
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.math.round
import kotlin.time.Duration

sealed interface YearEndReviewViewState {
    data object Loading : YearEndReviewViewState

    data object Error : YearEndReviewViewState

    data class Data(
        private val review: YearEndReview,
        private val mostListenedToPodcasts: List<Podcast>,
        val currentPage: Int = 1,
    ) : YearEndReviewViewState {
        private val pages =
            buildList {
                // Listening since
                add(
                    PageInfo.ListeningSince(review.listeningSince),
                )
                // Total episodes
                add(
                    PageInfo.TotalEpisodesListened(review.totalEpisodesListened),
                )
                // Listened duration
                add(
                    PageInfo.ListenedDuration(review.totalDurationListened),
                )
                // Consumed duration
                val averageSpeed =
                    if (review.totalDurationListened.compareTo(Duration.ZERO) == 0) {
                        1.0
                    } else {
                        review.totalConsumedDuration.div(review.totalDurationListened)
                    }
                if (averageSpeed > MIN_SPEED_FOR_CONSUMED_DURATION) {
                    add(
                        PageInfo.ConsumedDuration(
                            duration = review.totalConsumedDuration,
                            speed = averageSpeed,
                        ),
                    )
                }
                // Most listened podcasts
                add(
                    PageInfo.MostListenedToPodcasts(mostListenedToPodcasts),
                )
                // Most listened day
                add(
                    PageInfo.MostListenedDay(
                        day = review.mostListenedDate.date,
                        duration = review.mostListenedDate.duration,
                    ),
                )
                // Most listened month
                add(
                    PageInfo.MostListenedMonth(
                        month = review.mostListenedMonth.month,
                        duration = review.mostListenedMonth.duration,
                    ),
                )
                // Most listened day of week
                add(
                    PageInfo.MostListenedDayOfWeek(
                        dayOfWeek = review.mostListenedOnDayOfWeek.dayOfWeek,
                        duration = review.mostListenedOnDayOfWeek.duration,
                    ),
                )
                add(
                    PageInfo.Bye,
                )
            }

        val totalPages = pages.size

        sealed interface PageInfo {
            data class ListeningSince(val time: LocalDateTime) : PageInfo

            data class MostListenedToPodcasts(val podcasts: List<Podcast>) : PageInfo

            data class ListenedDuration(val duration: Duration) : PageInfo

            data class ConsumedDuration(
                val duration: Duration,
                private val speed: Double,
            ) : PageInfo {
                val speedRounded = round(speed * 10) / 10.0
            }

            data class TotalEpisodesListened(val episodes: Int) : PageInfo

            data class MostListenedDayOfWeek(
                val dayOfWeek: DayOfWeek,
                val duration: Duration,
            ) : PageInfo

            data class MostListenedDay(
                val day: LocalDate,
                val duration: Duration,
            ) : PageInfo

            data class MostListenedMonth(
                val month: Month,
                val duration: Duration,
            ) : PageInfo

            data object Bye : PageInfo
        }

        fun getPageInfo(): PageInfo {
            return pages.getOrNull(currentPage - 1) ?: error("Invalid page")
        }
    }

    companion object {
        private const val MIN_SPEED_FOR_CONSUMED_DURATION = 1.1
    }
}
