package com.ramitsuri.podcasts.navigation

import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack

class Navigator(startRoute: Route? = null) {
    private val _backstack = NavBackStack(setOfNotNull(Route.Home, startRoute).toMutableStateList())
    val backstack: List<Route>
        get() = _backstack

    val currentDestination: Route
        get() = _backstack.last()

    fun navigate(route: Route): Boolean {
        if (_backstack.last() == route) {
            return false
        }
        if (route is TopLevelRoute) {
            // This is a top level route, clear its backstack and add the route to it.
            _backstack.clear()
            _backstack.add(Route.Home)
            if (route != _backstack.last()) {
                _backstack.add(route)
            }
        } else {
            val index = _backstack.indexOf(route)
            if (index == -1) {
                // Doesn't exist in backstack, add it
                _backstack.add(route)
            } else {
                // Entry exists in backstack, remove all entries after it. Essentially, pop back to excluding
                for (i in (index + 1).._backstack.lastIndex) {
                    _backstack.removeAt(i)
                }
            }
        }
        return true
    }

    fun goBack() {
        _backstack.removeLastOrNull()
    }
}
