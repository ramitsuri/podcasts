package com.ramitsuri.podcasts.utils

interface Logger {
    fun toggleRemoteLogging(enable: Boolean)

    fun d(tag: String, message: String)

    fun v(tag: String, message: String)
}
