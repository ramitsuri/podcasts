package com.ramitsuri.podcasts.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.podcasts.android.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Composable
fun friendlyPublishDate(
    publishedDateTime: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
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
    val monthNames = monthNames
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

private val monthNames: MonthNames
    @Composable
    get() = MonthNames(
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
        ),
    )
