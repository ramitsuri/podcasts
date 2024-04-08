package com.ramitsuri.podcasts.android.navigation

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Route(val value: String) {
    HOME("home"),
    IMPORT_SUBSCRIPTIONS("import_subscriptions"),
    EXPLORE("explore"),
    LIBRARY("library"),
    EPISODE_DETAILS("episode_details"),
    QUEUE("queue"),
    PODCAST_DETAILS("podcast_details"),
    SUBSCRIPTIONS("subscriptions"),
    DOWNLOADS("downloads"),
    FAVORITES("favorites"),
    EPISODE_HISTORY("episode_history"),
    ;

    private fun routeWithArgValue(argValues: Map<RouteArgs, String>): String {
        return if (this == EPISODE_DETAILS) {
            value.plus("/${argValues[RouteArgs.EPISODE_ID]}")
        } else if (this == PODCAST_DETAILS) {
            value.plus("/${argValues[RouteArgs.PODCAST_ID]}/${argValues[RouteArgs.REFRESH_PODCAST]}")
        } else {
            value
        }
    }

    fun routeWithArgName(): String {
        return if (this == EPISODE_DETAILS) {
            value.plus("/{${RouteArgs.EPISODE_ID.value}}")
        } else if (this == PODCAST_DETAILS) {
            value.plus("/{${RouteArgs.PODCAST_ID.value}}/{${RouteArgs.REFRESH_PODCAST.value}}")
        } else {
            value
        }
    }

    fun navArgs(): List<NamedNavArgument> {
        return if (this == EPISODE_DETAILS) {
            listOf(
                navArgument(RouteArgs.EPISODE_ID.value) {
                    type = NavType.StringType
                    nullable = false
                },
            )
        } else if (this == PODCAST_DETAILS) {
            listOf(
                navArgument(RouteArgs.PODCAST_ID.value) {
                    type = NavType.LongType
                    nullable = false
                },
                navArgument(RouteArgs.REFRESH_PODCAST.value) {
                    type = NavType.BoolType
                    nullable = false
                },
            )
        } else {
            listOf()
        }
    }

    companion object {
        fun episodeDetails(episodeId: String): String {
            val encoded = Uri.encode(episodeId)
            return EPISODE_DETAILS.routeWithArgValue(mapOf(RouteArgs.EPISODE_ID to encoded))
        }

        fun podcastDetails(podcastId: Long, refreshPodcast: Boolean): String {
            return PODCAST_DETAILS.routeWithArgValue(
                mapOf(
                    RouteArgs.PODCAST_ID to podcastId.toString(),
                    RouteArgs.REFRESH_PODCAST to refreshPodcast.toString(),
                ),
            )
        }
    }
}
