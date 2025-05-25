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
    SHOULD_DOWNLOAD_ON_WIFI_ONLY("should_download_on_wifi_only"),
    IS_DUPLICATE_QUEUE_POSITIONS_FIXED("is_duplicate_queue_positions_fixed"),
    HAS_SEEN_WIDGET_ITEM("has_seen_widget_item"),
    LAST_TRENDING_PODCASTS_FETCH_TIME("last_trending_podcasts_fetch_time"),
    TRENDING_PODCASTS_LANGUAGES("trending_podcasts_languages"),
    TRENDING_PODCASTS_CATEGORIES("trending_podcasts_categories"),
    ;

    companion object {
        fun fromStringKey(key: String) = entries.firstOrNull { it.value == key }
    }
}
