package com.ramitsuri.podcasts.android.media

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.Player.State
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
import com.ramitsuri.podcasts.android.MainActivity
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import java.util.UUID
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
class PodcastMediaSessionService : MediaSessionService(), KoinComponent {
    private val cache by inject<Cache>()
    private val episodesRepository by inject<EpisodesRepository>()
    private val sessionHistoryRepository by inject<SessionHistoryRepository>()
    private val settings by inject<Settings>()
    private val clock: Clock by inject<Clock>()

    private var mediaSession: MediaSession? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val currentlyPlayingEpisode = MutableStateFlow<Episode?>(null)
    private var attemptingToPlayNextMedia = false

    private var insertStartActionJob: Job? = null
    private var insertStopActionJob: Job? = null

    override fun onCreate() {
        LogHelper.d(TAG, "onCreate")
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
        // TODO make available
        val saveToFavoritesButton =
            CommandButton.Builder()
                .setDisplayName(getString(R.string.save_to_favorites))
                .setIconResId(R.drawable.ic_favorite)
                .setSessionCommand(SessionCommand(ACTION_SAVE_TO_FAVORITES, Bundle.EMPTY))
                .build()
        val customLayout = listOf(replay10Button, skip30Button)

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
                .setId(UUID.randomUUID().toString())
                .setCallback(MediaSessionCallback())
                .setCustomLayout(customLayout)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                )
                .build()

        launchSuspend {
            settings.setPlayingState(PlayingState.NOT_PLAYING)
        }
        // Setting currentlyPlayingEpisode sooner so that it's available for session tracking later
        launchSuspend {
            episodesRepository.getCurrentEpisode().collectLatest { currentEpisode ->
                currentlyPlayingEpisode.update { currentEpisode }
            }
        }

        LogHelper.v(TAG, "Create with media session id: ${mediaSession?.id}")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        LogHelper.d(TAG, "onStartCommand")
        attachPlayerListener()
        startListeningForUpdates()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startListeningForUpdates() {
        launchSuspend {
            currentlyPlayingEpisode.collectLatest { currentEpisode ->
                val player = mediaSession?.player
                if (currentEpisode != null && player?.duration != null) {
                    val duration = player.duration
                    // Set episode's duration to what the player is reporting
                    if (duration != C.TIME_UNSET && currentEpisode.duration?.toLong() != (duration / 1000)) {
                        episodesRepository.updateDuration(currentEpisode.id, (duration / 1000).toInt())
                    }
                }
                while (true) {
                    if (player != null && player.isPlaying &&
                        currentEpisode != null && !currentEpisode.isCompleted &&
                        player.currentMediaItem?.mediaId == currentEpisode.id
                    ) {
                        val progressInSeconds = player.currentPosition.div(1000)
                        if (progressInSeconds != 0L) {
                            episodesRepository.updatePlayProgress(currentEpisode.id, progressInSeconds.toInt())
                        }
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
                val session = mediaSession
                if (session != null) {
                    val player = session.player
                    player.setPlaybackSpeed(speed)
                    sessionHistoryRepository.speedChange(player.isPlaying, session.id, player.playbackParameters.speed)
                }
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
        LogHelper.d(TAG, "onTaskRemoved")
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        LogHelper.d(TAG, "onDestroy")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        coroutineScope.cancel()
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
                    // .add(SessionCommand(ACTION_SAVE_TO_FAVORITES, Bundle.EMPTY))
                    .build()
            val playerCommands =
                ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .remove(Player.COMMAND_SEEK_TO_NEXT)
                    .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
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
            return coroutineScope.future {
                val currentEpisode = currentlyPlayingEpisode.value
                if (currentEpisode != null) {
                    val startingPosition = currentEpisode.progressInSeconds.times(1000).toLong()
                    MediaSession.MediaItemsWithStartPosition(
                        listOf(
                            currentEpisode.asMediaItem(artworkUriOverride = currentEpisode.cachedArtworkUri),
                        ),
                        C.INDEX_UNSET,
                        startingPosition,
                    )
                } else {
                    throw UnsupportedOperationException()
                }
            }
        }

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val keyEvent = intent.extras?.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                val currentPosition = session.player.currentPosition
                session.player.seekTo(currentPosition + 30_000)
                return true
            }
            if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                val currentPosition = session.player.currentPosition
                session.player.seekTo(currentPosition - 10_000)
                return true
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_SKIP_30) {
                val currentPosition = session.player.currentPosition
                session.player.seekTo(currentPosition + 30_000)
            }
            if (customCommand.customAction == ACTION_REPLAY_10) {
                val currentPosition = session.player.currentPosition
                session.player.seekTo(currentPosition - 10_000)
            }
            if (customCommand.customAction == ACTION_SAVE_TO_FAVORITES) {
                // TODO handle
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private suspend fun onEpisodePlayed(
        episode: Episode?,
        session: MediaSession?,
    ) {
        if (episode == null) {
            LogHelper.v(TAG, "Episode played but episode is null")
            return
        }
        if (session == null) {
            LogHelper.v(TAG, "Episode played but media session is null")
            return
        }
        insertStartActionJob?.cancel()
        val speed = session.player.playbackParameters.speed
        insertStartActionJob =
            coroutineScope.launch(Dispatchers.IO) {
                delay(500)
                sessionHistoryRepository.episodeStart(
                    episode = episode,
                    sessionId = session.id,
                    speed = speed,
                )
            }
    }

    private fun onEpisodePaused(
        episode: Episode?,
        session: MediaSession?,
    ) {
        if (episode == null) {
            LogHelper.v(TAG, "Episode paused but episode is null")
            return
        }
        if (session == null) {
            LogHelper.v(TAG, "Episode paused but media session is null")
            return
        }
        insertStopActionJob?.cancel()
        val speed = session.player.playbackParameters.speed
        insertStopActionJob =
            coroutineScope.launch(Dispatchers.IO) {
                delay(500)
                sessionHistoryRepository.episodeStop(
                    episode = episode,
                    sessionId = session.id,
                    speed = speed,
                )
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
                if (state == PlayingState.PLAYING) {
                    onEpisodePlayed(currentlyPlayingEpisode.value, mediaSession)
                } else if (state == PlayingState.NOT_PLAYING) {
                    onEpisodePaused(currentlyPlayingEpisode.value, mediaSession)
                }
            }
        }

        override fun onPlaybackStateChanged(
            @State playbackState: Int,
        ) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                playNextFromQueueOnMediaEnded(player)
            } else if (playbackState == STATE_BUFFERING) {
                settings.setPlayingState(PlayingState.LOADING)
            } else if (playbackState == STATE_READY) {
                if (player.isPlaying) {
                    settings.setPlayingState(PlayingState.PLAYING)
                } else {
                    settings.setPlayingState(PlayingState.NOT_PLAYING)
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
                coroutineScope.launch {
                    val nextEpisode = episodesRepository.getEpisode(mediaItem.mediaId) ?: return@launch
                    val position = nextEpisode.progressInSeconds.times(1000).toLong()
                    player.seekTo(position)
                }
            }
        }
    }

    // Almost replicated in EpisodeControllerImpl
    private fun playNextFromQueueOnMediaEnded(player: Player) {
        LogHelper.d(TAG, "Finding next media to play")
        if (attemptingToPlayNextMedia) {
            LogHelper.d(TAG, "Already attempting to play next media")
            return
        }
        attemptingToPlayNextMedia = true
        launchSuspend {
            delay(500)
            val currentlyPlayingEpisode = currentlyPlayingEpisode.value
            if (currentlyPlayingEpisode == null) {
                LogHelper.d(TAG, "Currently playing episode is null")
                attemptingToPlayNextMedia = false
                return@launchSuspend
            }

            suspend fun onDone() {
                attemptingToPlayNextMedia = false
                episodesRepository.markPlayed(currentlyPlayingEpisode.id)
            }

            val autoPlayNextInQueue = settings.autoPlayNextInQueue().first()
            if (!autoPlayNextInQueue) {
                LogHelper.d(TAG, "Auto play next in queue is false")
                onDone()
                return@launchSuspend
            }

            val sleepTimer = settings.getSleepTimerFlow().first()
            if (sleepTimer is SleepTimer.EndOfEpisode) {
                LogHelper.d(TAG, "Sleep timer is set to end of episode")
                settings.setSleepTimer(SleepTimer.None)
                onDone()
                return@launchSuspend
            }

            val queue = episodesRepository.getQueue()
            val currentEpisodeIndex = queue.indexOfFirst { it.id == currentlyPlayingEpisode.id }
            if (currentEpisodeIndex == -1) {
                LogHelper.v(TAG, "current episode not found in queue")
                onDone()
                return@launchSuspend
            }

            val nextEpisode = queue.getOrNull(currentEpisodeIndex + 1)
            if (nextEpisode == null) {
                LogHelper.v(TAG, "next episode is null")
                onDone()
                return@launchSuspend
            }

            LogHelper.d(TAG, "Found next media: ${nextEpisode.title}")
            val position = nextEpisode.progressInSeconds.times(1000L)
            player.setMediaItem(
                nextEpisode.asMediaItem(artworkUriOverride = nextEpisode.cachedArtworkUri),
                position,
            )
            player.play()
            episodesRepository.setCurrentlyPlayingEpisodeId(nextEpisode.id)
            onDone()
        }
    }

    private fun launchSuspend(block: suspend () -> Unit): Job {
        return coroutineScope.launch {
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
