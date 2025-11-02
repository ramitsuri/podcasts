package com.ramitsuri.podcasts.utils

interface Logger {
    fun d(
        tag: String,
        message: String,
    )

    fun v(
        tag: String,
        message: String,
    )
}
