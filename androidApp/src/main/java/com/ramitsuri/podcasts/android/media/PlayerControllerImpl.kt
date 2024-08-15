package com.ramitsuri.podcasts.android.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

class PlayerControllerImpl(
    context: Context,
    private val shouldAutoPlayNextInQueue: suspend () -> Boolean,
    private val getSleepTimer: suspend () -> SleepTimer,
    private val getAppQueue: suspend () -> List<Episode>,
    private val getCurrentlyPlayingEpisode: suspend () -> Episode?,
    private val coroutineScope: CoroutineScope,
) : PlayerController {
    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var updateQueueJob: Job? = null

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

    override fun play(episode: Episode) {
        if (controller?.currentMediaItem?.mediaId == episode.id) {
            controller?.play()
            updateQueue()
            return
        }
        controller?.setMediaItemForEpisode(episode)
        controller?.prepare()
        if (episode.progressInSeconds != 0) {
            controller?.seekTo(episode.progressInSeconds * 1000L)
        }
        controller?.play()
        updateQueue()
    }

    override fun updateQueue() {
        updateQueueJob?.cancel()
        updateQueueJob = coroutineScope.launch {
            delay(300)
            val currentEpisode = getCurrentlyPlayingEpisode()
            val playerQueue = if (currentEpisode == null ||
                currentEpisode.queuePosition == Episode.NOT_IN_QUEUE ||
                shouldAutoPlayNextInQueue().not() ||
                getSleepTimer() is SleepTimer.EndOfEpisode
            ) {
                emptyList()
            } else {
                getAppQueue().filter { it.queuePosition > currentEpisode.queuePosition }
            }
            removeEverythingButCurrentEpisode()
            addEpisodes(playerQueue)
            logQueue()
        }
    }

    override fun logQueue(): List<String> {
        return (controller?.let {
            (0 until it.mediaItemCount).map { index ->
                "$index: ${it.getMediaItemAt(index).mediaMetadata.title.toString()}\n"
            }
        } ?: listOf("Controller is null")).also {
            LogHelper.d(TAG, "$it")
        }
    }

    private fun removeEverythingButCurrentEpisode() {
        controller?.let {
            val currentIndex = it.currentMediaItemIndex
            for (index in (it.mediaItemCount - 1) downTo 0) {
                if (index != currentIndex) {
                    it.removeMediaItem(index)
                }
            }
        }
    }

    private fun addEpisodes(episodes: List<Episode>) {
        controller?.let {
            for (queueEpisode in episodes) {
                it.addMediaItem(queueEpisode.asMediaItem())
            }
        }
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

    override fun playNext() {
        controller?.seekToNextMediaItem()
    }

    override fun hasNext(): Boolean {
        return controller?.hasNextMediaItem() == true
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
