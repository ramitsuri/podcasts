package com.ramitsuri.podcasts.network.model

import kotlinx.datetime.Instant

internal data class GetEpisodesRequest(val id: Long, val sinceTime: Instant)
