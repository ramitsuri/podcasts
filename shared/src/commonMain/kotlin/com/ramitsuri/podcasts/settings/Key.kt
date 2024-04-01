package com.ramitsuri.podcasts.settings

internal enum class Key(val value: String) {
    CURRENTLY_PLAYING_EPISODE_ID("currently_playing_episode_id"),
    PLAYBACK_SPEED("playback_speed"),
    TRIM_SILENCE("trim_silence"),
    STOP_AFTER_END_OF_EPISODE("stop_after_end_of_episode"),
    STOP_AT_TIME("stop_at_time"),
    LAST_EPISODE_FETCH_TIME("last_episode_fetch_time"),
    ;
}
