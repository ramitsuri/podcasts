package com.ramitsuri.podcasts.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            if (BottomNavItem.entries.map { it.route }.contains(currentDestination)) {
                BottomNavBar(
                    selectedTabRoute = currentDestination,
                    onHomeTabClicked = {
                        navController.navigateToMainDestination(BottomNavItem.HOME)
                    },
                    onExploreClicked = {
                        navController.navigateToMainDestination(BottomNavItem.EXPLORE)
                    },
                    onLibraryClicked = {
                        navController.navigateToMainDestination(BottomNavItem.LIBRARY)
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.HOME.route,
            modifier = modifier.padding(innerPadding),
        ) {
            composable(route = BottomNavItem.HOME.route) {
                Text(text = "Home")
            }
            composable(route = BottomNavItem.EXPLORE.route) {
                Text(text = "Explore")
            }
            composable(route = BottomNavItem.LIBRARY.route) {
                Text(text = "Library")
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTabRoute: String?,
    onHomeTabClicked: () -> Unit,
    onExploreClicked: () -> Unit,
    onLibraryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = item.route == selectedTabRoute
            NavBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = stringResource(id = item.labelRes),
                    )
                },
                label = {
                    Text(
                        stringResource(item.labelRes),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                    )
                },
                selected = selected,
                onClick = {
                    when (item) {
                        BottomNavItem.HOME -> onHomeTabClicked()
                        BottomNavItem.EXPLORE -> onExploreClicked()
                        BottomNavItem.LIBRARY -> onLibraryClicked()
                    }
                },
            )
        }
    }
}

private fun NavHostController.navigateToMainDestination(to: BottomNavItem) {
    val currentDestination = currentBackStackEntry?.destination?.route
    if (currentDestination == to.route) {
        return
    }
    navigate(to.route) {
        // So that pressing back from any main bottom tab item leads user to home tab first
        popUpTo(BottomNavItem.HOME.route)
        launchSingleTop = true
    }
}
