package com.ramitsuri.podcasts.model

data class PodcastRefreshResult(
    val autoDownloadableEpisodes:List<Episode>,
    val otherEpisodes: List<Episode>,
)
