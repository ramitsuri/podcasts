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

    override fun play(episode: Episode) {
        controller?.setMediaItemForEpisode(episode)
        controller?.prepare()
        if (episode.progressInSeconds != 0) {
            controller?.seekTo(episode.progressInSeconds * 1000L)
        }
        controller?.play()
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
