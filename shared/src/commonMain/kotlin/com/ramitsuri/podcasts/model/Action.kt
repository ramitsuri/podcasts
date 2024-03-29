package com.ramitsuri.podcasts.model

enum class Action(val value: String) {
    START("start"),
    STOP("stop"),
    ;

    companion object {
        fun fromValue(value: String): Action {
            return entries.firstOrNull { it.value == value } ?: START
        }
    }
}
