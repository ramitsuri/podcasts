package com.ramitsuri.podcasts.android.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.Route.EPISODE_DETAILS
import com.ramitsuri.podcasts.navigation.Route.PODCAST_DETAILS
import com.ramitsuri.podcasts.navigation.RouteArgs

fun Route.navArgs(): List<NamedNavArgument> {
    return if (this == EPISODE_DETAILS) {
        listOf(
            navArgument(RouteArgs.EPISODE_ID.value) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(RouteArgs.PODCAST_ID.value) {
                // String even though this is a long because cannot have nullable long in nav args
                type = NavType.StringType
                nullable = true
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
