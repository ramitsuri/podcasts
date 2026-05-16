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
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlayerControllerImpl(
    context: Context,
    private val episodesRepository: EpisodesRepository,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope,
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
                    updateQueue()
                },
                MoreExecutors.directExecutor(),
            )
    }

    override fun play(episode: Episode) {
        if (controller?.currentMediaItem?.mediaId != episode.id) {
            controller?.setMediaItemForEpisode(episode)
            controller?.prepare()
            if (episode.progressInSeconds != 0) {
                controller?.seekTo(episode.progressInSeconds * 1000L)
            }
        }
        controller?.play()
    }

    override fun playCurrentEpisode() {
        coroutineScope.launch {
            val episode = getCurrentEpisode()
            if (episode != null) {
                if (episode.isCompleted) {
                    episodesRepository.markNotPlayed(episode.id)
                }
                play(episode)
            } else {
                LogHelper.v(TAG, "Current episode is null")
            }
        }
    }

    private fun updateQueue() {
        coroutineScope.launch {
            combine(
                episodesRepository.getCurrentEpisode().distinctUntilChangedBy { it?.id },
                settings.autoPlayNextInQueue(),
                settings.getSleepTimerFlow().map { it is SleepTimer.EndOfEpisode }.distinctUntilChanged(),
                episodesRepository.getQueueFlow().distinctUntilChangedBy { queue -> queue.map { it.id } },
            ) { currentEpisode, isAutoPlayOn, isEndOfEpisodeTimer, appQueue ->
                val appQueueIds = appQueue.map { it.id }
                val restOfTheQueue =
                    when {
                        !isAutoPlayOn || isEndOfEpisodeTimer -> {
                            emptyList()
                        }

                        currentEpisode == null -> {
                            appQueue
                        }

                        currentEpisode.id in appQueueIds -> {
                            // Use current episode from queue because currentEpisode flow provides updates here only
                            // if id changes
                            val currentEpisodeFromQueue = appQueue.first { it.id == currentEpisode.id }
                            appQueue.filter { it.queuePosition > currentEpisodeFromQueue.queuePosition }
                        }

                        else -> {
                            emptyList()
                        }
                    }.sortedBy { it.queuePosition }
                currentEpisode to restOfTheQueue
            }.collect { (currentEpisode, appQueue) ->
                controller?.let { controller ->
                    if (currentEpisode != null) {
                        if (controller.currentMediaItem == null) {
                            controller.setMediaItemForEpisode(currentEpisode)
                        } else {
                            val currentIndex = controller.currentMediaItemIndex
                            if (controller.mediaItemCount > currentIndex + 1) {
                                controller.removeMediaItems(currentIndex + 1, controller.mediaItemCount)
                            }
                        }
                    } else {
                        controller.clearMediaItems()
                    }
                    controller.addMediaItems(appQueue.map { it.asMediaItem() })
                }
                logQueue()
            }
        }
    }

    private fun logQueue() {
        (
            controller?.let {
                (0 until it.mediaItemCount).joinToString("\n") { index ->
                    "$index: ${it.getMediaItemAt(index).mediaMetadata.title}"
                }
            } ?: "Controller is null"
        ).let {
            LogHelper.d(TAG, it)
        }
    }

    override fun pause() {
        controller?.pause()
    }

    override suspend fun seek(byPercentOfDuration: Float) {
        seek(SeekType.Percent(byPercentOfDuration))
    }

    override suspend fun skip() {
        val by = 30.seconds
        seek(SeekType.Absolute(by))
    }

    override suspend fun replay() {
        val by = (-10).seconds
        seek(SeekType.Absolute(by))
    }

    private suspend fun seek(type: SeekType) {
        if (controller?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) != true) {
            LogHelper.d(TAG, "Seek requested but not allowed")
            return
        }
        val episode = getCurrentEpisode()
        if (episode == null) {
            LogHelper.v(TAG, "Seek requested but episode is null")
            return
        }
        if (episode.isCompleted) {
            episodesRepository.markNotPlayed(episode.id)
        }

        val newPlayProgress =
            when (type) {
                is SeekType.Absolute -> {
                    episode.progressInSeconds.seconds + type.to
                }

                is SeekType.Percent -> {
                    val duration = episode.duration?.seconds
                    if (duration == null) {
                        LogHelper.v(TAG, "Seek requested but no duration")
                        return
                    }
                    duration.times(type.percent.toDouble())
                }
            }
                .inWholeSeconds
                .coerceIn(0, controller?.duration ?: 0)
        episodesRepository.updatePlayProgress(episode.id, newPlayProgress.toInt())
        controller?.seekTo(newPlayProgress * 1000) // This is in milliseconds
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

    private suspend fun getCurrentEpisode() = episodesRepository.getCurrentEpisode().first()

    private sealed interface SeekType {
        data class Absolute(val to: Duration) : SeekType

        data class Percent(val percent: Float) : SeekType
    }

    companion object {
        private const val TAG = "PlayerController"
    }
}
