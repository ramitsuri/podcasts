package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.PodcastWithEpisodes

data class PodcastDetailsViewState(
    val podcastWithEpisodes: PodcastWithEpisodes? = null,
    val currentlyPlayingEpisodeId: String? = null,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
)
