package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

internal class EpisodeControllerImpl(
    private val longLivingScope: CoroutineScope,
    private val episodesRepository: EpisodesRepository,
    private val playerController: PlayerController,
    private val episodeDownloader: EpisodeDownloader,
    private val settings: Settings,
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
            val currentlyPlayingEpisode = episodesRepository.getCurrentEpisode().firstOrNull()
            if (currentlyPlayingEpisode?.id == episodeId) {
                // Play next only if the currently playing episode is marked as played, otherwise we'd be changing
                // the media even when the user didn't intend to do that
                playNextFromQueueOnMediaEnded(currentlyPlayingEpisode)
            }
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

    // Almost replicated in PodcastMediaSessionService
    private suspend fun playNextFromQueueOnMediaEnded(currentlyPlayingEpisode: Episode?) {
        LogHelper.d(TAG, "Finding next media to play")

        if (currentlyPlayingEpisode == null) {
            LogHelper.d(TAG, "Currently playing episode is null")
            return
        }

        fun onDone() {
            playerController.pause()
        }

        val playingState = settings.getPlayingStateFlow().first()
        if (playingState == PlayingState.NOT_PLAYING) {
            LogHelper.d(TAG, "Currently not playing")
            onDone()
            return
        }

        val autoPlayNextInQueue = settings.autoPlayNextInQueue().first()
        if (!autoPlayNextInQueue) {
            LogHelper.d(TAG, "Auto play next in queue is false")
            onDone()
            return
        }

        val sleepTimer = settings.getSleepTimerFlow().first()
        if (sleepTimer is SleepTimer.EndOfEpisode) {
            LogHelper.d(TAG, "Sleep timer is set to end of episode")
            settings.setSleepTimer(SleepTimer.None)
            onDone()
            return
        }

        val queue = episodesRepository.getQueue()
        val currentEpisodeIndex = queue.indexOfFirst { it.id == currentlyPlayingEpisode.id }
        if (currentEpisodeIndex == -1) {
            LogHelper.v(TAG, "current episode not found in queue")
            onDone()
            return
        }

        val nextEpisode = queue.getOrNull(currentEpisodeIndex + 1)
        if (nextEpisode == null) {
            LogHelper.v(TAG, "next episode is null")
            onDone()
            return
        }

        LogHelper.d(TAG, "Found next media: ${nextEpisode.title}")
        onDone()
        playEpisode(nextEpisode)
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
