package com.ramitsuri.podcasts.model

data class PodcastWithEpisodes(
    val podcast: Podcast,
    val episodes: List<Episode>,
)
