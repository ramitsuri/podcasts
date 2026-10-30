package com.ramitsuri.podcasts.utils

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class TestClock : Clock {
    var timeZone: TimeZone = TimeZone.UTC
    var nowLocal: String = Instant.DISTANT_PAST.toLocalDateTime(timeZone).toString()

    override fun now(): Instant {
        return LocalDateTime.parse(nowLocal).toInstant(timeZone)
    }
}
