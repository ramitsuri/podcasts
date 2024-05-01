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
    val page: Long = 1,
    val availableEpisodeCount: Long = 0,
)

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
            newPodcastWithEpisodes: PodcastWithEpisodes?
        ): PodcastWithSelectableEpisodes? {
            if (newPodcastWithEpisodes == null) {
                return this
            }
            if (this == null) {
                return PodcastWithSelectableEpisodes(newPodcastWithEpisodes)
            }
            val currentEpisodes = this.episodes
            val trulyNewEpisodes = newPodcastWithEpisodes.episodes.toMutableList()
            val newEpisodes = buildList {
                addAll(
                    currentEpisodes.map { currentSelectableEpisode ->
                        val updatedCurrentEpisode = newPodcastWithEpisodes
                            .episodes
                            .firstOrNull { it.id == currentSelectableEpisode.episode.id }
                        if (updatedCurrentEpisode == null) {
                            // Episode was not updated, so retain current selection and episode
                            currentSelectableEpisode
                        } else {
                            // Episode that we already know of was updated, so remove it from truly new episodes
                            // (episodes that we would append to the bottom of the list)
                            trulyNewEpisodes.remove(updatedCurrentEpisode)
                            // Episode was updated, so update episode but retain previous selection
                            currentSelectableEpisode.copy(episode = updatedCurrentEpisode)
                        }
                    },
                )
                addAll(trulyNewEpisodes.map { SelectableEpisode(it) })
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
