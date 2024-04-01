package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class EpisodeControllerImpl(
    private val longLivingScope: CoroutineScope,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
    private val episodeDownloader: EpisodeDownloader,
) : EpisodeController {
    override fun onEpisodePlayClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.setCurrentlyPlayingEpisodeId(episode.id)
            playerController.play(episode)
        }
    }

    override fun onEpisodePauseClicked() {
        playerController.pause()
    }

    override fun onEpisodeAddToQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.addToQueue(episode.id)
        }
    }

    override fun onEpisodeRemoveFromQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.removeFromQueue(episode.id)
        }
    }

    override fun onEpisodeDownloadClicked(episode: Episode) {
        episodeDownloader.add(episode)
    }

    override fun onEpisodeRemoveDownloadClicked(episode: Episode) {
        episodeDownloader.remove(episode)
    }

    override fun onEpisodeCancelDownloadClicked(episode: Episode) {
        episodeDownloader.cancel(episode)
    }

    override fun onEpisodePlayedClicked(episodeId: String) {
        longLivingScope.launch {
            episodesRepository.markPlayed(episodeId)
        }
    }

    override fun onEpisodeNotPlayedClicked(episodeId: String) {
        longLivingScope.launch {
            episodesRepository.markNotPlayed(episodeId)
        }
    }

    override fun onEpisodeMarkFavorite(episodeId: String) {
        longLivingScope.launch {
            episodesRepository.updateFavorite(id = episodeId, isFavorite = true)
        }
    }

    override fun onEpisodeMarkNotFavorite(episodeId: String) {
        longLivingScope.launch {
            episodesRepository.updateFavorite(id = episodeId, isFavorite = false)
        }
    }
}
