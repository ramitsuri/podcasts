package com.ramitsuri.podcasts.model

sealed class PodcastError(private val throwable: Throwable? = null) {
    data object NoInternet : PodcastError()

    class Unknown(throwable: Throwable?) : PodcastError(throwable)

    class BadRequest(throwable: Throwable? = null) : PodcastError(throwable)

    fun exceptionMessage(): String? {
        return throwable?.message
    }
}
