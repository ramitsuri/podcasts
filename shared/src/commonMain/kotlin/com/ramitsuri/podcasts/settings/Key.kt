package com.ramitsuri.podcasts.settings

internal enum class Key(val value: String) {
    CURRENTLY_PLAYING_EPISODE_ID("currently_playing_episode_id"),
    PLAYBACK_SPEED("playback_speed"),
    TRIM_SILENCE("trim_silence"),
    STOP_AFTER_END_OF_EPISODE("stop_after_end_of_episode"),
    STOP_AT_TIME("stop_at_time"),
    LAST_EPISODE_FETCH_TIME("last_episode_fetch_time"),
    AUTO_PLAY_NEXT_IN_QUEUE("auto_play_next_in_queue"),
    PODCAST_DETAILS_EPISODE_SORT_ORDER("podcast_details_episode_sort_order"),
    REMOVE_COMPLETED_EPISODES_AFTER("remove_completed_episodes_after"),
    REMOVE_UNFINISHED_EPISODES_AFTER("remove_unfinished_episodes_after"),
    REMOTE_LOGGING_ENABLED("remote_logging_enabled"),
    ;

    companion object {
        fun fromStringKey(key: String) = entries.firstOrNull { it.value == key }
    }
}
