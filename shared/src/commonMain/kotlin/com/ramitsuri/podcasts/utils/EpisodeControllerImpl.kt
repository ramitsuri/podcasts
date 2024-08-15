package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

internal class EpisodeControllerImpl(
    private val longLivingScope: CoroutineScope,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
    private val episodeDownloader: EpisodeDownloader,
) : EpisodeController {
    override fun onEpisodePlayClicked(episode: Episode) {
        longLivingScope.launch {
            playEpisode(episode)
        }
    }

    override fun onEpisodePauseClicked() {
        playerController.pause()
    }

    override fun onEpisodeAddToQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.addToQueue(episode.id)
            playerController.updateQueue()
        }
    }

    override fun onEpisodeRemoveFromQueueClicked(episode: Episode) {
        longLivingScope.launch {
            episodesRepository.removeFromQueue(episode.id)
            playerController.updateQueue()
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
            val currentlyPlayingEpisode = episodesRepository.getCurrentEpisode().firstOrNull()
            if (currentlyPlayingEpisode?.id == episodeId) {
                if (playerController.hasNext()) {
                    playerController.playNext()
                } else {
                    playerController.pause()
                }
            }
            episodesRepository.markPlayed(episodeId)
            playerController.updateQueue()
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

    private suspend fun playEpisode(episode: Episode) {
        episodesRepository.setCurrentlyPlayingEpisodeId(episode.id)
        if (episode.isCompleted) {
            episodesRepository.markNotPlayed(episode.id)
        }
        playerController.play(episode)
    }

    companion object {
        private const val TAG = "EpisodeController"
    }
}
