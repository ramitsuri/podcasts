package com.ramitsuri.podcasts.android.navigation

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
    ;

    fun routeWithArgValue(argValue: String?): String {
        return if (this == EPISODE_DETAILS) {
            value.plus("/$argValue")
        } else if (this == PODCAST_DETAILS) {
            value.plus("/$argValue")
        } else {
            value
        }
    }

    fun routeWithArgName(): String {
        return if (this == EPISODE_DETAILS) {
            value.plus("/{${RouteArgs.EPISODE_ID.value}}")
        } else if (this == PODCAST_DETAILS) {
            value.plus("/{${RouteArgs.PODCAST_ID.value}}")
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
            )
        } else {
            listOf()
        }
    }
}
