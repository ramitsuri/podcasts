package com.ramitsuri.podcasts.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object LogHelper : KoinComponent {
    private val logger: Logger by inject<Logger>()

    fun d(
        tag: String,
        message: String,
    ) {
        logger.d(tag, message)
    }

    fun v(
        tag: String,
        message: String,
    ) {
        logger.v(tag, message)
    }
}
