package com.ramitsuri.podcasts.model

enum class PlayingState(val value: String) {
    PLAYING("playing"),
    NOT_PLAYING("not_playing"),
    LOADING("loading"),
    ;

    companion object {
        fun fromValue(value: String?): PlayingState {
            return entries.firstOrNull { it.value == value } ?: NOT_PLAYING
        }
    }
}
