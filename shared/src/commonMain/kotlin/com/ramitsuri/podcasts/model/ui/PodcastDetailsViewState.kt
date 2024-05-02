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
    val page: Long = 1,
    val availableEpisodeCount: Long = 0,
    val hasMorePages: Boolean = true,
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

    constructor(podcastWithEpisodes: PodcastWithEpisodes) : this(
        podcast = podcastWithEpisodes.podcast,
        episodes = podcastWithEpisodes.episodes.map { SelectableEpisode(it) },
    )

    companion object {
        fun PodcastWithSelectableEpisodes?.mergeWithNew(
            newPodcastWithEpisodes: PodcastWithEpisodes?,
        ): PodcastWithSelectableEpisodes? {
            if (newPodcastWithEpisodes == null) {
                return this
            }
            if (this == null) {
                return PodcastWithSelectableEpisodes(newPodcastWithEpisodes)
            }
            val currentEpisodes = this.episodes
            val newEpisodes =
                newPodcastWithEpisodes.episodes.map { episode ->
                    SelectableEpisode(
                        episode = episode,
                        selected = currentEpisodes.firstOrNull { it.episode.id == episode.id }?.selected ?: false,
                    )
                }
            return PodcastWithSelectableEpisodes(newPodcastWithEpisodes.podcast, newEpisodes)
        }
    }
}

data class SelectableEpisode(
    val selected: Boolean,
    val episode: Episode,
) {
    constructor(episode: Episode) : this(selected = false, episode = episode)
}
