package com.ramitsuri.podcasts.network.model

internal data class GetEpisodesRequest(val id: Long, val sinceEpochSeconds: Long)
