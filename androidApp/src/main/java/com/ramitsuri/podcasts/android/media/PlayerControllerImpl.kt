package com.ramitsuri.podcasts.android.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration

class PlayerControllerImpl(
    private val longLivingScope: CoroutineScope,
    private val settings: Settings,
    context: Context,
) : PlayerController {
    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun initializePlayer() {
        val sessionToken = SessionToken(appContext, ComponentName(appContext, PodcastMediaSessionService::class.java))
        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture
            ?.addListener(
                {
                    controller = controllerFuture?.get()
                    launchSuspend {
                        setPlaybackSpeed(settings.getPlaybackSpeed())
                    }
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

    override fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    override fun replay(by: Duration) {
        controller?.let {
            val newPosition = (it.currentPosition - by.inWholeMilliseconds).coerceAtLeast(0)
            it.seekTo(newPosition)
        }
    }

    override fun skip(by: Duration) {
        controller?.let {
            val newPosition = (it.currentPosition + by.inWholeMilliseconds).coerceAtMost(it.duration)
            it.seekTo(newPosition)
        }
    }

    override fun seek(to: Duration) {
        val newPosition = to.inWholeMilliseconds
        controller?.seekTo(newPosition)
    }

    override fun releasePlayer() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun MediaController.setMediaItemForEpisode(episode: Episode) {
        setMediaItem(episode.asMediaItem(), episode.progressInSeconds.times(1000).toLong())
    }

    private fun launchSuspend(block: suspend () -> Unit) {
        longLivingScope.launch {
            block()
        }
    }
}
