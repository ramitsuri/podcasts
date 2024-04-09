package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.utils.Constants
import kotlinx.datetime.Instant

data class SettingsViewState(
    val autoPlayNextInQueue: Boolean = true,
    val lastFetchTime: Instant = Constants.NEVER_FETCHED_TIME,
    val fetching: Boolean = false,
)
