package com.ramitsuri.podcasts.settings

import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class Settings internal constructor(private val keyValueStore: KeyValueStore) {
    private val playingState: MutableStateFlow<PlayingState> = MutableStateFlow(PlayingState.NOT_PLAYING)

    internal fun getCurrentEpisodeId(): Flow<String?> {
        return keyValueStore.getStringFlow(Key.CURRENTLY_PLAYING_EPISODE_ID, null)
    }

    internal suspend fun setCurrentlyPlayingEpisodeId(episodeId: String?) {
        keyValueStore.putString(Key.CURRENTLY_PLAYING_EPISODE_ID, episodeId)
    }

    fun getPlaybackSpeedFlow(): Flow<Float> {
        return keyValueStore
            .getFloatFlow(Key.PLAYBACK_SPEED, 1f)
            .map { it ?: 1f }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        keyValueStore.putFloat(Key.PLAYBACK_SPEED, speed)
    }

    fun getPlayingStateFlow(): Flow<PlayingState> {
        return playingState
    }

    fun setPlayingState(playingState: PlayingState) {
        this.playingState.update { playingState }
    }

    fun getTrimSilenceFlow(): Flow<Boolean> {
        return keyValueStore
            .getBooleanFlow(Key.TRIM_SILENCE, false)
    }

    suspend fun setTrimSilence(trim: Boolean) {
        keyValueStore.putBoolean(Key.TRIM_SILENCE, trim)
    }

    fun getSleepTimerFlow(): Flow<SleepTimer> {
        return combine(
            keyValueStore.getBooleanFlow(Key.STOP_AFTER_END_OF_EPISODE, false),
            keyValueStore.getStringFlow(Key.STOP_AT_TIME, null),
        ) { stopAtEndOfEpisode, stopAtTimeString ->
            Pair(stopAtEndOfEpisode, stopAtTimeString)
        }.map { (stopAtEndOfEpisode, stopAtTimeString) ->
            if (stopAtTimeString != null) {
                val instant = runCatching { Instant.parse(stopAtTimeString) }.getOrNull()
                if (instant != null) {
                    SleepTimer.Custom(instant)
                } else {
                    SleepTimer.None
                }
            } else if (stopAtEndOfEpisode) {
                SleepTimer.EndOfEpisode
            } else {
                SleepTimer.None
            }
        }
    }

    suspend fun setSleepTimer(sleepTimer: SleepTimer) {
        when (sleepTimer) {
            is SleepTimer.Custom -> {
                keyValueStore.putBoolean(Key.STOP_AFTER_END_OF_EPISODE, false)
                keyValueStore.putString(Key.STOP_AT_TIME, sleepTimer.time.toString())
            }

            is SleepTimer.EndOfEpisode -> {
                keyValueStore.putBoolean(Key.STOP_AFTER_END_OF_EPISODE, true)
                keyValueStore.putString(Key.STOP_AT_TIME, null)
            }

            is SleepTimer.None -> {
                keyValueStore.putBoolean(Key.STOP_AFTER_END_OF_EPISODE, false)
                keyValueStore.putString(Key.STOP_AT_TIME, null)
            }
        }
    }

    suspend fun getLastEpisodeFetchTime(): Instant {
        val stringTime = keyValueStore.getStringFlow(Key.LAST_EPISODE_FETCH_TIME, null).first()
        return if (stringTime == null) {
            Constants.NEVER_FETCHED_TIME
        } else {
            runCatching { Instant.parse(stringTime) }.getOrNull() ?: Constants.NEVER_FETCHED_TIME
        }
    }

    suspend fun setLastEpisodeFetchTime(time: Instant = Clock.System.now()) {
        keyValueStore.putString(Key.LAST_EPISODE_FETCH_TIME, time.toString())
    }

    fun autoPlayNextInQueue(): Flow<Boolean> {
        return keyValueStore.getBooleanFlow(Key.AUTO_PLAY_NEXT_IN_QUEUE, true)
    }

    suspend fun setAutoPlayNextInQueue(autoPlayNextInQueue: Boolean) {
        keyValueStore.putBoolean(Key.AUTO_PLAY_NEXT_IN_QUEUE, autoPlayNextInQueue)
    }
}
