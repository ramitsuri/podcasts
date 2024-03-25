package com.ramitsuri.podcasts.model

sealed interface PodcastError {
    data class NoInternet(val throwable: Throwable) : PodcastError

    data class Unknown(val throwable: Throwable?) : PodcastError

    data class BadRequest(val throwable: Throwable? = null) : PodcastError
}
