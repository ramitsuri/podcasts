package com.ramitsuri.podcasts.settings

import com.ramitsuri.podcasts.model.PlayingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Settings internal constructor(private val keyValueStore: KeyValueStore) {
    internal fun getCurrentEpisodeId(): Flow<String?> {
        return keyValueStore.getStringFlow(Key.CURRENTLY_PLAYING_EPISODE_ID, null)
    }

    internal suspend fun setCurrentlyPlayingEpisodeId(episodeId: String?) {
        keyValueStore.putString(Key.CURRENTLY_PLAYING_EPISODE_ID, episodeId)
    }

    suspend fun getPlaybackSpeed(): Float {
        return getPlaybackSpeedFlow().first()
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
        return keyValueStore
            .getStringFlow(Key.PLAYING_STATE, null)
            .map { value ->
                PlayingState.fromValue(value)
            }
    }

    suspend fun setPlayingState(playingState: PlayingState) {
        keyValueStore.putString(Key.PLAYING_STATE, playingState.value)
    }
}
