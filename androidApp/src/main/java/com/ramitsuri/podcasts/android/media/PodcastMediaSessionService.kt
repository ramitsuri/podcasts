package com.ramitsuri.podcasts.android.media

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.State
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
class PodcastMediaSessionService : MediaSessionService(), KoinComponent {
    private val cache by inject<Cache>()
    private val episodesRepository by inject<EpisodesRepository>()
    private val longLivingScope by inject<CoroutineScope>()
    private val settings by inject<Settings>()
    private val clock: Clock by inject<Clock>()

    private var mediaSession: MediaSession? = null
    private val currentlyPlayingEpisode = MutableStateFlow<Episode?>(null)

    override fun onCreate() {
        super.onCreate()

        val replay10Button =
            CommandButton.Builder()
                .setDisplayName(getString(R.string.replay_10))
                .setIconResId(R.drawable.ic_replay_10)
                .setSessionCommand(SessionCommand(ACTION_REPLAY_10, Bundle.EMPTY))
                .build()
        val skip30Button =
            CommandButton.Builder()
                .setDisplayName(getString(R.string.skip_30))
                .setIconResId(R.drawable.ic_skip_30)
                .setSessionCommand(SessionCommand(ACTION_SKIP_30, Bundle.EMPTY))
                .build()
        val saveToFavoritesButton =
            CommandButton.Builder()
                .setDisplayName(getString(R.string.save_to_favorites))
                .setIconResId(R.drawable.ic_favorite)
                .setSessionCommand(SessionCommand(ACTION_SAVE_TO_FAVORITES, Bundle.EMPTY))
                .build()
        val customLayout = listOf(replay10Button, skip30Button, saveToFavoritesButton)

        val audioAttributes =
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build()
        val cacheFactory =
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setCacheWriteDataSinkFactory(null)
        val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheFactory)
        val audioOnlyRenderersFactory =
            RenderersFactory { handler, _, audioListener, _, _ ->
                arrayOf(MediaCodecAudioRenderer(this, MediaCodecSelector.DEFAULT, handler, audioListener))
            }
        val player =
            ExoPlayer.Builder(this, audioOnlyRenderersFactory, mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build()

        mediaSession =
            MediaSession.Builder(this, player)
                .setCallback(MediaSessionCallback())
                .setCustomLayout(customLayout)
                .build()

        launchSuspend {
            settings.setPlayingState(PlayingState.NOT_PLAYING)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        attachPlayerListener()
        startListeningForUpdates()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startListeningForUpdates() {
        launchSuspend {
            episodesRepository.getCurrentEpisode().collectLatest { currentEpisode ->
                currentlyPlayingEpisode.update { currentEpisode }
                val player = mediaSession?.player
                currentlyPlayingEpisode.value?.let { episode ->
                    val duration = player?.duration ?: return@let
                    // Set episode's duration to what the player is reporting
                    if (duration != C.TIME_UNSET && episode.duration?.toLong() != (duration / 1000)) {
                        episodesRepository.updateDuration(episode.id, (duration / 1000).toInt())
                    }
                }
                while (true) {
                    val episodeId = player?.currentMediaItem?.mediaId ?: break
                    val progressInSeconds = player.currentPosition.div(1000)
                    if (progressInSeconds != 0L && player.isPlaying) {
                        episodesRepository.updatePlayProgress(episodeId, progressInSeconds.toInt())
                    }
                    delay(1.seconds)
                }
            }
        }
        launchSuspend {
            settings.getTrimSilenceFlow().collectLatest { trimSilence ->
                val player = mediaSession?.player as? ExoPlayer
                player?.skipSilenceEnabled = trimSilence
            }
        }
        launchSuspend {
            settings.getPlaybackSpeedFlow().collectLatest { speed ->
                val player = mediaSession?.player
                player?.setPlaybackSpeed(speed)
            }
        }
        launchSuspend {
            var job: Job? = null
            settings.getSleepTimerFlow().collectLatest { sleepTimer ->
                job?.cancel()
                job =
                    coroutineScope {
                        launch {
                            if (sleepTimer is SleepTimer.Custom) {
                                delay(sleepTimer.time.minus(clock.now()))
                                mediaSession?.player?.pause()
                                settings.setSleepTimer(SleepTimer.None)
                            }
                        }
                    }
            }
        }
    }

    private fun attachPlayerListener() {
        val player = mediaSession?.player as? ExoPlayer
        player?.addListener(PlayerListener(player))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // TODO investigate if this is going to be called since I only add one media at a time
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        longLivingScope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ConnectionResult {
            val sessionCommands =
                SessionCommands.Builder()
                    .add(SessionCommand(ACTION_REPLAY_10, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SKIP_30, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SAVE_TO_FAVORITES, Bundle.EMPTY))
                    .build()
            val playerCommands =
                ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()

            return ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return longLivingScope.future {
                val currentEpisode = currentlyPlayingEpisode.value
                if (currentEpisode != null) {
                    val startingPosition = currentEpisode.progressInSeconds.times(1000).toLong()
                    MediaSession.MediaItemsWithStartPosition(
                        listOf(currentEpisode.asMediaItem()),
                        C.INDEX_UNSET,
                        startingPosition,
                    )
                } else {
                    throw UnsupportedOperationException()
                }
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_SKIP_30) {
                val currentPosition = mediaSession?.player?.currentPosition
                if (currentPosition != null) {
                    mediaSession?.player?.seekTo(currentPosition + 30_000)
                }
            }
            if (customCommand.customAction == ACTION_REPLAY_10) {
                val currentPosition = mediaSession?.player?.currentPosition
                if (currentPosition != null) {
                    mediaSession?.player?.seekTo(currentPosition - 10_000)
                }
            }
            if (customCommand.customAction == ACTION_SAVE_TO_FAVORITES) {
                Log.d(TAG, "Save to favorites")
                // TODO handle
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private inner class PlayerListener(private val player: Player) : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            val state =
                if (isPlaying) {
                    PlayingState.PLAYING
                } else {
                    if (player.playbackState == STATE_BUFFERING) {
                        PlayingState.LOADING
                    } else {
                        PlayingState.NOT_PLAYING
                    }
                }
            launchSuspend {
                settings.setPlayingState(state)
            }
        }

        override fun onPlaybackStateChanged(
            @State playbackState: Int,
        ) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                playNextFromQueueOnMediaEnded(player)
            } else if (playbackState == STATE_BUFFERING && player.isPlaying) {
                launchSuspend {
                    settings.setPlayingState(PlayingState.LOADING)
                }
            }
        }

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int,
        ) {
            currentlyPlayingEpisode.value?.let { episode ->
                val isAboutToPlay = player.playbackState == STATE_BUFFERING || playWhenReady
                val duration = episode.duration
                if (duration != null) {
                    val hasReachedEndOfEpisode =
                        abs((duration.toLong() * 1000) - player.currentPosition) <= 1000L &&
                            episode.progressInSeconds == duration
                    if (isAboutToPlay && hasReachedEndOfEpisode) {
                        player.seekTo(0L)
                        launchSuspend {
                            episodesRepository.updatePlayProgress(
                                id = episode.id,
                                playProgressInSeconds = 0,
                            )
                        }
                    }
                }
            }
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            if (mediaItem != null) {
                longLivingScope.launch {
                    val nextEpisode = episodesRepository.getEpisode(mediaItem.mediaId) ?: return@launch
                    val position = nextEpisode.progressInSeconds.times(1000).toLong()
                    player.seekTo(position)
                }
            }
        }
    }

    private fun playNextFromQueueOnMediaEnded(player: Player) {
        launchSuspend {
            val sleepTimer = settings.getSleepTimerFlow().first()
            if (sleepTimer is SleepTimer.EndOfEpisode) {
                settings.setSleepTimer(SleepTimer.None)
                return@launchSuspend
            }
            val queue = episodesRepository.getQueue()
            val currentlyPlayingEpisode = currentlyPlayingEpisode.value
            if (currentlyPlayingEpisode != null) {
                episodesRepository.markPlayed(currentlyPlayingEpisode.id)
            }
            val currentEpisodeIndex = queue.indexOfFirst { it.id == currentlyPlayingEpisode?.id }
            if (currentEpisodeIndex == -1) {
                // TODO log with logger
                Log.d(TAG, "current episode not found")
                return@launchSuspend
            }
            val nextEpisode = queue.getOrNull(currentEpisodeIndex + 1)
            if (nextEpisode == null) {
                // TODO log with logger
                Log.d(TAG, "next episode is null")
                return@launchSuspend
            }

            val position = nextEpisode.progressInSeconds.times(1000L)
            player.setMediaItem(nextEpisode.asMediaItem(), position)
            episodesRepository.setCurrentlyPlayingEpisodeId(nextEpisode.id)
        }
    }

    private fun launchSuspend(block: suspend () -> Unit) {
        longLivingScope.launch {
            block()
        }
    }

    companion object {
        private const val TAG = "MediaSessionService"
        private const val ACTION_REPLAY_10 = "com.ramitsuri.podcasts.session_command.replay_10"
        private const val ACTION_SKIP_30 = "com.ramitsuri.podcasts.session_command.skip_30"
        private const val ACTION_SAVE_TO_FAVORITES = "com.ramitsuri.podcasts.session_command.save_to_favorites"
    }
}
