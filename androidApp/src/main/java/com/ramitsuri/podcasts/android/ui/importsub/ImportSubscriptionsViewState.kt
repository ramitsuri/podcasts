package com.ramitsuri.podcasts.android.ui.importsub

import com.ramitsuri.podcasts.model.Podcast

data class ImportSubscriptionsViewState(
    val isLoading: Boolean = true,
    val podcasts: List<Podcast> = listOf(),
    val subscribed: Boolean = false,
)
