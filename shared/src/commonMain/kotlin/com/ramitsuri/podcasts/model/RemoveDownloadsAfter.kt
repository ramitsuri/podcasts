package com.ramitsuri.podcasts.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class RemoveDownloadsAfter(val key: Int, val duration: Duration) {
    TWENTY_FOUR_HOURS(key = 0, duration = 24.hours),
    SEVEN_DAYS(key = 1, duration = 7.days),
    THIRTY_DAYS(key = 2, duration = 30.days),
    NINETY_DAYS(key = 3, duration = 90.days),
    ;

    companion object {
        fun fromKey(key: Int): RemoveDownloadsAfter {
            return entries.firstOrNull { it.key == key } ?: THIRTY_DAYS
        }
    }
}
