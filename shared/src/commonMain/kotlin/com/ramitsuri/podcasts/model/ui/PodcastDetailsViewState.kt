package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast

data class PodcastDetailsViewState(
    val podcastWithEpisodes: PodcastWithSelectableEpisodes? = null,
    val currentlyPlayingEpisodeId: String? = null,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
    val page: Long = 1,
    val availableEpisodeCount: Long = 0,
    val hasMorePages: Boolean = true,
    val loadingOlderEpisodes: Boolean = false,
    val searchTerm: String = "",
) {
    val episodeSortOrder: EpisodeSortOrder
        get() = podcastWithEpisodes?.podcast?.episodeSortOrder ?: EpisodeSortOrder.default
}

data class PodcastWithSelectableEpisodes(
    val podcast: Podcast,
    val episodes: List<SelectableEpisode>,
) {
    val inSelectionState = episodes.any { it.selected }

    val selectedCount = episodes.count { it.selected }
}

data class SelectableEpisode(
    val selected: Boolean,
    val episode: Episode,
)
