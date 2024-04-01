package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.model.Episode

interface EpisodeController {
    fun onEpisodePlayClicked(episode: Episode)

    fun onEpisodePauseClicked()

    fun onEpisodeAddToQueueClicked(episode: Episode)

    fun onEpisodeRemoveFromQueueClicked(episode: Episode)

    fun onEpisodeDownloadClicked(episode: Episode)

    fun onEpisodeRemoveDownloadClicked(episode: Episode)

    fun onEpisodeCancelDownloadClicked(episode: Episode)

    fun onEpisodePlayedClicked(episodeId: String)

    fun onEpisodeNotPlayedClicked(episodeId: String)

    fun onEpisodeMarkFavorite(episodeId: String)

    fun onEpisodeMarkNotFavorite(episodeId: String)
}
