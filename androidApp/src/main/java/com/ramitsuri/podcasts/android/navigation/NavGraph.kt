package com.ramitsuri.podcasts.android.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.podcasts.android.ui.episode.EpisodeDetailsScreen
import com.ramitsuri.podcasts.android.ui.home.HomeScreen
import com.ramitsuri.podcasts.android.ui.importsub.ImportSubscriptionsScreen
import com.ramitsuri.podcasts.android.ui.importsub.ImportSubscriptionsViewModel
import com.ramitsuri.podcasts.android.ui.library.LibraryScreen
import com.ramitsuri.podcasts.android.ui.library.queue.QueueScreen
import com.ramitsuri.podcasts.android.ui.player.PlayerScreen
import com.ramitsuri.podcasts.android.ui.player.PlayerViewModel
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
import com.ramitsuri.podcasts.viewmodel.QueueViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    val scaffoldSheetState = rememberBottomSheetScaffoldState()
    // TODO use this to hide/show the bottom tabs
    val offsetBottomSheet by remember(scaffoldSheetState.bottomSheetState) {
        derivedStateOf {
            runCatching { scaffoldSheetState.bottomSheetState.requireOffset() }.getOrDefault(0F)
        }
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            if (scaffoldSheetState.bottomSheetState.currentValue != SheetValue.Expanded) {
                BottomNavBar(
                    modifier =
                    Modifier
                        .offset { IntOffset(x = 0, y = 0) },
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
        val playerViewModel =
            viewModel<PlayerViewModel>(
                factory = PlayerViewModel.factory(),
            )
        val playerState by playerViewModel.state.collectAsStateWithLifecycle()
        var peekHeightPx by remember { mutableIntStateOf(0) }
        val bottomSheetVisible = playerState.hasEverBeenPlayed
        val bottomPadding =
            if (bottomSheetVisible) {
                with(LocalDensity.current) {
                    innerPadding.calculateBottomPadding() + peekHeightPx.toDp()
                }
            } else {
                0.dp
            }
        BottomSheetScaffold(
            scaffoldState = scaffoldSheetState,
            sheetPeekHeight = bottomPadding,
            modifier = Modifier.padding(if (bottomSheetVisible) innerPadding else PaddingValues(bottom = 0.dp)),
            sheetDragHandle = { },
            sheetContent =
            if (bottomSheetVisible) {
                {
                    PlayerScreen(
                        isExpanded = scaffoldSheetState.bottomSheetState.currentValue == SheetValue.Expanded,
                        state = playerState,
                        onNotExpandedHeightKnown = {
                            peekHeightPx = it
                        },
                        onGoToQueueClicked = { },
                        onReplayClicked = playerViewModel::onReplayRequested,
                        onPauseClicked = playerViewModel::onPauseClicked,
                        onPlayClicked = playerViewModel::onPlayClicked,
                        onSkipClicked = playerViewModel::onSkipRequested,
                        onSeekValueChange = playerViewModel::onSeekRequested,
                        onPlaybackSpeedSet = playerViewModel::onSpeedChangeRequested,
                        onPlaybackSpeedIncrease = playerViewModel::onSpeedIncreaseRequested,
                        onPlaybackSpeedDecrease = playerViewModel::onSpeedDecreaseRequested,
                        onToggleTrimSilence = playerViewModel::toggleTrimSilence,
                        onSleepTimer = playerViewModel::onSleepTimerRequested,
                        onSleepTimerIncrease = playerViewModel::onSleepTimerIncreaseRequested,
                        onSleepTimerDecrease = playerViewModel::onSleepTimerDecreaseRequested,
                    )
                }
            } else {
                { }
            },
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.HOME.route.value,
                modifier = modifier.padding(innerPadding),
            ) {
                composable(route = BottomNavItem.HOME.route.value) {
                    val viewModel = koinViewModel<HomeViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    HomeScreen(
                        state = state,
                        onImportSubscriptionsClicked = {
                            navController.navigate(Route.IMPORT_SUBSCRIPTIONS.value)
                        },
                        onEpisodeClicked = {
                            val encoded = Uri.encode(it)
                            navController.navigate(Route.EPISODE_DETAILS.routeWithArgValue(encoded))
                        },
                        onEpisodePlayClicked = viewModel::onEpisodePlayClicked,
                        onEpisodePauseClicked = viewModel::onEpisodePauseClicked,
                        onEpisodeAddToQueueClicked = viewModel::onEpisodeAddToQueueClicked,
                        onEpisodeRemoveFromQueueClicked = viewModel::onEpisodeRemoveFromQueueClicked,
                        onEpisodeDownloadClicked = viewModel::onEpisodeDownloadClicked,
                        onEpisodeRemoveDownloadClicked = viewModel::onEpisodeRemoveDownloadClicked,
                        onEpisodeCancelDownloadClicked = viewModel::onEpisodeCancelDownloadClicked,
                        onEpisodePlayedClicked = viewModel::onEpisodePlayedClicked,
                        onEpisodeNotPlayedClicked = viewModel::onEpisodeNotPlayedClicked,
                    )
                }

                composable(route = BottomNavItem.EXPLORE.route.value) {
                    Text(text = "Explore")
                }

                composable(route = BottomNavItem.LIBRARY.route.value) {
                    LibraryScreen(
                        onSubscriptionsClicked = { /*TODO*/ },
                        onQueueClicked = { navController.navigate(Route.QUEUE.value) },
                        onDownloadsClicked = { /*TODO*/ },
                        onHistoryClicked = { /*TODO*/ },
                    )
                }

                composable(route = Route.IMPORT_SUBSCRIPTIONS.value) {
                    val viewModel =
                        viewModel<ImportSubscriptionsViewModel>(
                            factory = ImportSubscriptionsViewModel.factory(),
                        )
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    ImportSubscriptionsScreen(
                        viewState = state,
                        onSubscriptionsDataFilePicked = viewModel::onSubscriptionDataFilePicked,
                        onSubscribeAllPodcasts = viewModel::subscribeAllPodcasts,
                        onSuggestionAccepted = viewModel::onSuggestionAccepted,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(
                    route = Route.EPISODE_DETAILS.routeWithArgName(),
                    arguments = Route.EPISODE_DETAILS.navArgs(),
                ) { backStackEntry ->
                    val episodeId = backStackEntry.arguments?.getString(RouteArgs.EPISODE_ID.value)
                    val decoded =
                        if (episodeId == null) {
                            null
                        } else {
                            Uri.decode(episodeId)
                        }
                    val viewModel = koinViewModel<EpisodeDetailsViewModel>(parameters = { parametersOf(decoded) })
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    EpisodeDetailsScreen(
                        state = state,
                        onBack = { navController.navigateUp() },
                        onEpisodePlayClicked = viewModel::onEpisodePlayClicked,
                        onEpisodePauseClicked = viewModel::onEpisodePauseClicked,
                        onEpisodeAddToQueueClicked = viewModel::onEpisodeAddToQueueClicked,
                        onEpisodeRemoveFromQueueClicked = viewModel::onEpisodeRemoveFromQueueClicked,
                        onEpisodeDownloadClicked = viewModel::onEpisodeDownloadClicked,
                        onEpisodeRemoveDownloadClicked = viewModel::onEpisodeRemoveDownloadClicked,
                        onEpisodeCancelDownloadClicked = viewModel::onEpisodeCancelDownloadClicked,
                        onEpisodePlayedClicked = viewModel::onEpisodePlayedClicked,
                        onEpisodeNotPlayedClicked = viewModel::onEpisodeNotPlayedClicked,
                    )
                }

                composable(route = Route.QUEUE.value) {
                    val viewModel = koinViewModel<QueueViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    QueueScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEpisodesRearranged = viewModel::onEpisodeRearrangementRequested,
                        onEpisodeClicked = {
                            val encoded = Uri.encode(it)
                            navController.navigate(Route.EPISODE_DETAILS.routeWithArgValue(encoded))
                        },
                        onEpisodePlayClicked = viewModel::onEpisodePlayClicked,
                        onEpisodePauseClicked = viewModel::onEpisodePauseClicked,
                        onEpisodeRemoveFromQueueClicked = viewModel::onEpisodeRemoveFromQueueClicked,
                        onEpisodeDownloadClicked = viewModel::onEpisodeDownloadClicked,
                        onEpisodeRemoveDownloadClicked = viewModel::onEpisodeRemoveDownloadClicked,
                        onEpisodeCancelDownloadClicked = viewModel::onEpisodeCancelDownloadClicked,
                        onEpisodePlayedClicked = viewModel::onEpisodePlayedClicked,
                        onEpisodeNotPlayedClicked = viewModel::onEpisodeNotPlayedClicked,
                    )
                }
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
            val selected = item.route.value == selectedTabRoute
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
    if (currentDestination == to.route.value) {
        return
    }
    navigate(to.route.value) {
        // So that pressing back from any main bottom tab item leads user to home tab first
        popUpTo(BottomNavItem.HOME.route.value)
        launchSingleTop = true
    }
}
