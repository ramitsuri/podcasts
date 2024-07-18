package com.ramitsuri.podcasts.android.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlin.time.Duration

class PlayerControllerImpl(
    context: Context,
) : PlayerController {
    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun initializePlayer() {
        LogHelper.d(TAG, "Initialize player")
        val sessionToken = SessionToken(appContext, ComponentName(appContext, PodcastMediaSessionService::class.java))
        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture
            ?.addListener(
                {
                    controller = controllerFuture?.get()
                },
                MoreExecutors.directExecutor(),
            )
    }

    override fun play(episode: Episode, queue: List<Episode>) {
        controller?.clearMediaItems()
        controller?.setMediaItemForEpisode(episode)
        controller?.prepare()
        if (episode.progressInSeconds != 0) {
            controller?.seekTo(episode.progressInSeconds * 1000L)
        }
        controller?.play()
        if (queue.isNotEmpty()) {
            addEpisodes(episode, queue)
        }
    }

    override fun addToQueue(episode: Episode) {
        if (!isPlayingOrAboutToPlay) {
            return
        }
        controller?.addMediaItem(episode.asMediaItem())
        logPlaylist()
    }

    override fun removeFromQueue(episode: Episode) {
        if (!isPlayingOrAboutToPlay) {
            return
        }
        val indexOfEpisode = indexOf(episode) ?: return
        controller?.removeMediaItem(indexOfEpisode)
        logPlaylist()
    }

    override fun swapInQueue(episode1: Episode, episode2: Episode) {
        if (!isPlayingOrAboutToPlay) {
            return
        }
        val index1 = indexOf(episode1) ?: return
        val index2 = indexOf(episode2) ?: return
        controller?.replaceMediaItem(index1, episode2.asMediaItem())
        controller?.replaceMediaItem(index2, episode1.asMediaItem())
        logPlaylist()
    }

    fun addEpisodesAfterCurrentMediaItem(episodes: List<Episode>) {
        resetQueueFromCurrentMediaItem()
        controller?.let {
            it.addMediaItems(episodes.map { episode -> episode.asMediaItem() })
        }
    }

    private fun resetQueueFromCurrentMediaItem() {
        controller?.let {
            for (index in it.currentMediaItemIndex until it.mediaItemCount) {
                it.removeMediaItem(index)
            }
        }
    }

    private val isPlayingOrAboutToPlay: Boolean
        get() = controller?.isPlaying == true || controller?.isLoading == true

    private fun logPlaylist() {
        controller?.let {
            for (index in 0 until it.mediaItemCount) {
                LogHelper.d(TAG, it.getMediaItemAt(index).mediaMetadata.title.toString())
            }
        }
    }

    private fun indexOf(episode: Episode): Int? {
        controller?.let {
            for (index in 0 until it.mediaItemCount) {
                if (it.getMediaItemAt(index).mediaId == episode.id) {
                    return index
                }
            }
        }
        return null
    }

    private fun addEpisodes(playingEpisode: Episode, queueEpisodes: List<Episode>) {
        var playingEpisodeEncounteredInQueue = false
        val nextEpisodes = queueEpisodes.filter {
            if (!playingEpisodeEncounteredInQueue && it.id == playingEpisode.id) {
                playingEpisodeEncounteredInQueue = true
            }
            playingEpisodeEncounteredInQueue
        }
        nextEpisodes.forEach { controller?.addMediaItem(it.asMediaItem()) }
    }

    private fun resetQueue(currentEpisode: Episode, queueEpisodes: List<Episode>) {
        if (!isPlayingOrAboutToPlay) {
            return
        }
        val controller = controller ?: return

    }

    override fun pause() {
        controller?.pause()
    }

    override fun seek(to: Duration) {
        if (controller?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) != true) {
            LogHelper.d(TAG, "Seek requested but not allowed")
            return
        }
        val newPosition = to.inWholeMilliseconds.coerceIn(0, controller?.duration ?: 0)
        controller?.seekTo(newPosition)
    }

    override fun releasePlayer() {
        LogHelper.d(TAG, "Release player")
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun MediaController.setMediaItemForEpisode(episode: Episode) {
        setMediaItem(
            episode.asMediaItem(artworkUriOverride = with(appContext) { episode.cachedArtworkUri }),
            episode.progressInSeconds.times(1000).toLong(),
        )
    }

    companion object {
        private const val TAG = "PlayerController"
    }
}
