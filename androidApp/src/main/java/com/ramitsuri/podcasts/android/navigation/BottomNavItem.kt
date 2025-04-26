package com.ramitsuri.podcasts.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.navigation.Route

enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes
    val labelRes: Int,
) {
    HOME(
        route = Route.HOME,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        labelRes = R.string.nav_home,
    ),
    EXPLORE(
        route = Route.EXPLORE,
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search,
        labelRes = R.string.nav_explore,
    ),
    LIBRARY(
        route = Route.LIBRARY,
        icon = Icons.Outlined.VideoLibrary,
        selectedIcon = Icons.Filled.VideoLibrary,
        labelRes = R.string.nav_library,
    ),
}
