package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastWithEpisodes

data class PodcastDetailsViewState(
    val podcastWithEpisodes: PodcastWithSelectableEpisodes? = null,
    val currentlyPlayingEpisodeId: String? = null,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
    val episodeSortOrder: EpisodeSortOrder = EpisodeSortOrder.DATE_PUBLISHED_DESC,
)

data class PodcastWithSelectableEpisodes(
    val podcast: Podcast,
    val episodes: List<SelectableEpisode>,
) {
    val inSelectionState = episodes.any { it.selected }

    val selectedCount = episodes.count { it.selected }

    constructor(podcastWithEpisodes: PodcastWithEpisodes) : this(
        podcast = podcastWithEpisodes.podcast,
        episodes = podcastWithEpisodes.episodes.map { SelectableEpisode(selected = false, episode = it) },
    )
}

data class SelectableEpisode(
    val selected: Boolean,
    val episode: Episode,
)
