package com.ramitsuri.podcasts.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.podcasts.android.R
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.temporal.ChronoUnit

@Composable
fun friendlyPublishDate(
    publishedDateTime: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val durationSincePublished = now - publishedDateTime
    val hours = durationSincePublished.inWholeHours
    val days = durationSincePublished.inWholeDays
    when {
        hours < 2 -> {
            return stringResource(id = R.string.one_hour_ago)
        }

        hours < 24 -> {
            return stringResource(id = R.string.hours_ago_format, hours)
        }

        days < 2 -> {
            return stringResource(id = R.string.one_day_ago)
        }

        days < 7 -> {
            return stringResource(id = R.string.days_ago_format, days)
        }
    }
    val monthNames = monthNames()
    val format =
        LocalDateTime.Format {
            monthName(monthNames)
            char(' ')
            dayOfMonth()
            if (now.toLocalDateTime(timeZone).year != publishedDateTime.toLocalDateTime(timeZone).year) {
                char(',')
                char(' ')
                year()
            }
        }
    return publishedDateTime
        .toLocalDateTime(timeZone)
        .format(format)
}

@Composable
fun friendlyFetchDateTime(
    fetchDateTime: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val monthNames = monthNames()
    val am = stringResource(id = R.string.am)
    val pm = stringResource(id = R.string.pm)
    val format =
        LocalDateTime.Format {
            amPmHour()
            char(':')
            minute()
            char(' ')
            amPmMarker(am = am, pm = pm)
            char(' ')
            monthName(monthNames)
            char(' ')
            dayOfMonth()
            if (now.toLocalDateTime(timeZone).year != fetchDateTime.toLocalDateTime(timeZone).year) {
                char(',')
                char(' ')
                year()
            }
        }
    return fetchDateTime
        .toLocalDateTime(timeZone)
        .format(format)
}

@Composable
fun minutesFormatted(
    minutes: Long,
    suffix: String,
): String {
    val hours = minutes / 60
    val formatted =
        when {
            minutes < 60 -> {
                stringResource(id = R.string.play_state_button_min, minutes)
            }

            else -> {
                val minutesRemaining = minutes - (hours * 60)
                if (minutesRemaining == 0L) {
                    stringResource(id = R.string.play_state_button_hour, hours)
                } else {
                    stringResource(id = R.string.play_state_button_hour_and_min, hours, minutesRemaining)
                }
            }
        }
    return formatted + suffix
}

@Composable
fun dateFormatted(
    toFormat: LocalDate,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    useShortMonthNames: Boolean = true,
): String {
    val nowLocalDateTime = LocalDateTime(now.toLocalDateTime(timeZone).date, LocalTime(0, 0))
    val toFormatDateTime = toFormat.atTime(0, 0)
    val daysBetweenNowAndToFormat =
        Duration.between(
            nowLocalDateTime.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS),
            toFormatDateTime.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS),
        ).toDays()
    return when (daysBetweenNowAndToFormat) {
        0L -> {
            stringResource(id = R.string.episode_history_today)
        }

        -1L -> {
            stringResource(id = R.string.episode_history_yesterday)
        }

        else -> {
            val monthNames = monthNames(useShortMonthNames)
            val format =
                LocalDateTime.Format {
                    monthName(monthNames)
                    char(' ')
                    dayOfMonth()
                    if (nowLocalDateTime.year != toFormatDateTime.year) {
                        char(',')
                        char(' ')
                        year()
                    }
                }
            return toFormatDateTime
                .format(format)
        }
    }
}

@Composable
fun monthFormatted(
    month: Month,
    useShortMonthNames: Boolean = true,
): String {
    return monthNames(useShortMonthNames).names[month.value - 1]
}

@Composable
fun dayOfWeekFormatted(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> stringResource(R.string.mondays)
        java.time.DayOfWeek.TUESDAY -> stringResource(R.string.tuesdays)
        java.time.DayOfWeek.WEDNESDAY -> stringResource(R.string.wednesdays)
        java.time.DayOfWeek.THURSDAY -> stringResource(R.string.thursdays)
        java.time.DayOfWeek.FRIDAY -> stringResource(R.string.fridays)
        java.time.DayOfWeek.SATURDAY -> stringResource(R.string.saturdays)
        java.time.DayOfWeek.SUNDAY -> stringResource(R.string.sundays)
    }
}

@Composable
private fun monthNames(useShortNames: Boolean = true): MonthNames {
    return MonthNames(
        if (useShortNames) {
            listOf(
                stringResource(id = R.string.month_jan),
                stringResource(id = R.string.month_feb),
                stringResource(id = R.string.month_mar),
                stringResource(id = R.string.month_apr),
                stringResource(id = R.string.month_may),
                stringResource(id = R.string.month_jun),
                stringResource(id = R.string.month_jul),
                stringResource(id = R.string.month_aug),
                stringResource(id = R.string.month_sep),
                stringResource(id = R.string.month_oct),
                stringResource(id = R.string.month_nov),
                stringResource(id = R.string.month_dec),
            )
        } else {
            listOf(
                stringResource(id = R.string.month_jan_full),
                stringResource(id = R.string.month_feb_full),
                stringResource(id = R.string.month_mar_full),
                stringResource(id = R.string.month_apr_full),
                stringResource(id = R.string.month_may_full),
                stringResource(id = R.string.month_jun_full),
                stringResource(id = R.string.month_jul_full),
                stringResource(id = R.string.month_aug_full),
                stringResource(id = R.string.month_sep_full),
                stringResource(id = R.string.month_oct_full),
                stringResource(id = R.string.month_nov_full),
                stringResource(id = R.string.month_dec_full),
            )
        },
    )
}
