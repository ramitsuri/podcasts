package com.ramitsuri.podcasts.android.navigation

import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.TopLevelRoute

/**
 * startRoute should be set if it's different than Route.Home
 */
class Navigator(startRoute: Route? = null) {
    val backstack: NavBackStack<Route> = NavBackStack(setOfNotNull(Route.Home, startRoute).toMutableStateList())

    val currentDestination: Route
        get() = backstack.last()

    fun navigate(route: Route): Boolean {
        if (backstack.last() == route) {
            return false
        }
        if (route is TopLevelRoute) {
            // This is a top level route, clear its backstack and add the route to it.
            backstack.clear()
            backstack.add(Route.Home)
            if (route != backstack.last()) {
                backstack.add(route)
            }
        } else {
            val index = backstack.indexOf(route)
            if (index == -1) {
                // doesn't exist in backstack, add it
                backstack.add(route)
            } else {
                // Entry exists in backstack, remove all entries after it
                for (i in (index + 1)..backstack.lastIndex) {
                    backstack.removeAt(i)
                }
            }
        }
        return true
    }

    fun goBack() {
        backstack.removeLastOrNull()
    }
}

