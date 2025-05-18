package com.ramitsuri.podcasts.network.model

internal data class TrendingPodcastsRequest(
    val sinceEpochSeconds: Long,
    val maxResults: Int = 50,
)
