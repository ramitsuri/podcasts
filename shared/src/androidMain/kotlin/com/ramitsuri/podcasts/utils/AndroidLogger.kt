package com.ramitsuri.podcasts.utils

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class AndroidLogger(
    private var enableRemote: Boolean,
    private val deviceModel: String,
    private val isDebug: Boolean,
    private val clock: Clock = Clock.System,
) : Logger {
    private val deviceId: String = UUID.randomUUID().toString()

    override fun toggleRemoteLogging(enable: Boolean) {
        localLog(tag = TAG, message = "EnableRemote -> $enable")
        enableRemote = enable
    }

    override fun d(tag: String, message: String) {
        localLog(tag, message)
    }

    override fun v(tag: String, message: String) {
        localLog(tag, message)
        remoteLog(tag, message)
    }

    private fun localLog(tag: String, message: String) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }

    private fun remoteLog(tag: String, message: String) {
        if (isDebug) {
            return
        }
        val time = clock.now()
        if (enableRemote) {
            getDb(time).push().setValue(
                mapOf(
                    "tag" to tag,
                    "time" to formatLogTime(time),
                    "device" to deviceModel,
                    "message" to message,
                ),
            )
        }
    }

    private fun getDb(time: Instant): DatabaseReference {
        return Firebase.database.getReference("logs/$deviceId/${formatLogParent(time)}")
    }

    private fun formatLogTime(time: Instant): String {
        val format =
            LocalDateTime.Format {
                hour()
                char(':')
                minute()
                char(':')
                second()
                char('.')
                secondFraction(3)
            }
        return time
            .toLocalDateTime(TimeZone.UTC)
            .format(format)
    }

    private fun formatLogParent(time: Instant): String {
        val format =
            LocalDateTime.Format {
                year()
                char('-')
                monthNumber()
                char('-')
                dayOfMonth()
            }
        return time
            .toLocalDateTime(TimeZone.UTC)
            .format(format)
    }

    companion object {
        private const val TAG = "LogHelper"
    }
}
