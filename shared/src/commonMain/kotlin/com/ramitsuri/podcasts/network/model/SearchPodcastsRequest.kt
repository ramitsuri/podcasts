package com.ramitsuri.podcasts.network.model

data class SearchPodcastsRequest(val term: String, val findSimilar: Boolean = true, val maxResults: Int = 50)
