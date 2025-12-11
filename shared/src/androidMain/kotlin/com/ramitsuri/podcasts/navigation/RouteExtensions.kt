package com.ramitsuri.podcasts.navigation

import android.net.Uri
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.SharePodcastInfo

val Route.deepLinkWithArgValue: String?
    get() =
        when (this) {
            is Route.EpisodeDetails -> {
                DEEP_LINK_BASE_URL
                    .plus("?${RouteArgs.EPISODE_ID.value}")
                    .plus("=$episodeId")
                    .plus("&${RouteArgs.PODCAST_ID.value}")
                    .plus("=$podcastId")
            }

            else -> null
        }

val Route.EpisodeDetails.Companion.deepLinkWithArgName: String
    get() =
        DEEP_LINK_BASE_URL
            .plus("?${RouteArgs.EPISODE_ID.value}")
            .plus("={${RouteArgs.EPISODE_ID.value}}")
            .plus("&${RouteArgs.PODCAST_ID.value}")
            .plus("={${RouteArgs.PODCAST_ID.value}}")

fun Episode?.sharePodcastInfo(): SharePodcastInfo? {
    if (this == null) {
        return null
    }
    val url =
        Route.EpisodeDetails(
            episodeId = Uri.encode(id),
            podcastId = podcastId,
        ).deepLinkWithArgValue ?: return null
    return SharePodcastInfo(
        podcastName = podcastName,
        episodeTitle = title,
        url = url,
    )
}

private const val DEEP_LINK_BASE_URL = "https://ramitsuri.github.io/podcasts"
