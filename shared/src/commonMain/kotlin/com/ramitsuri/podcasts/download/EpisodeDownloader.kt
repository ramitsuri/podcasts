package com.ramitsuri.podcasts.download

import com.ramitsuri.podcasts.model.Episode

interface EpisodeDownloader {
    fun add(episode: Episode)

    fun remove(episode: Episode)

    fun cancel(episode: Episode)

    fun setAllowOnWifiOnly(onWifiOnly: Boolean)
}
