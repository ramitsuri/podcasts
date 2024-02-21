package com.ramitsuri.podcasts.model

sealed class PodcastResult<out S> {
    data class Success<S>(val data: S) : PodcastResult<S>()
    data class Failure(val error: PodcastError) : PodcastResult<Nothing>()
}