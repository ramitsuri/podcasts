package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.GetPodcast
import com.ramitsuri.podcasts.network.model.PodcastDto
import kotlinx.datetime.Instant

data class Podcast(
    val id: Long,
    val guid: String,
    val title: String,
    val description: String,
    val author: String,
    val owner: String,
    val url: String,
    val link: String,
    val image: String,
    val artwork: String,
    val explicit: Boolean,
    val episodeCount: Int,
    val categories: List<Category>,
    val subscribed: Boolean,
    val autoDownloadEpisodes: Boolean,
    val newEpisodeNotifications: Boolean,
    val subscribedDate: Instant?,
    val lastRefreshDate: Instant?,
) {
    internal constructor(dto: PodcastDto) : this(
        id = dto.id,
        guid = dto.guid,
        title = dto.title,
        description = dto.description,
        author = dto.author,
        owner = dto.owner,
        url = dto.url,
        link = dto.link,
        image = dto.image,
        artwork = dto.artwork,
        explicit = dto.explicit,
        episodeCount = dto.episodeCount,
        categories = dto.categories.map { Category(it) },
        subscribed = false,
        autoDownloadEpisodes = false,
        newEpisodeNotifications = false,
        subscribedDate = null,
        lastRefreshDate = null,
    )

    internal constructor(getPodcast: GetPodcast, categories: List<Category>) : this(
        id = getPodcast.id,
        guid = getPodcast.guid,
        title = getPodcast.title,
        description = getPodcast.description,
        author = getPodcast.author,
        owner = getPodcast.owner,
        url = getPodcast.url,
        link = getPodcast.link,
        image = getPodcast.image,
        artwork = getPodcast.artwork,
        explicit = getPodcast.explicit,
        episodeCount = getPodcast.episodeCount,
        categories = categories,
        subscribed = getPodcast.subscribed,
        autoDownloadEpisodes = getPodcast.autoDownloadEpisodes,
        newEpisodeNotifications = getPodcast.newEpisodeNotification,
        subscribedDate = getPodcast.subscribedDate,
        lastRefreshDate = getPodcast.lastRefreshDate,
    )
}