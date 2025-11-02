package com.ramitsuri.podcasts.utils

import android.util.Log

class AndroidLogger(
    private val isDebug: Boolean,
) : Logger {
    override fun d(
        tag: String,
        message: String,
    ) {
        localLog(tag, message)
    }

    override fun v(
        tag: String,
        message: String,
    ) {
        localLog(tag, message)
    }

    private fun localLog(
        tag: String,
        message: String,
    ) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
}
