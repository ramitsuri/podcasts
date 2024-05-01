package com.ramitsuri.podcasts.android.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.podcasts.android.ui.downloads.DownloadsScreen
import com.ramitsuri.podcasts.android.ui.episode.EpisodeDetailsScreen
import com.ramitsuri.podcasts.android.ui.favorites.FavoritesScreen
import com.ramitsuri.podcasts.android.ui.history.EpisodeHistoryScreen
import com.ramitsuri.podcasts.android.ui.home.HomeScreen
import com.ramitsuri.podcasts.android.ui.importsub.ImportSubscriptionsScreen
import com.ramitsuri.podcasts.android.ui.importsub.ImportSubscriptionsViewModel
import com.ramitsuri.podcasts.android.ui.library.LibraryScreen
import com.ramitsuri.podcasts.android.ui.library.queue.QueueScreen
import com.ramitsuri.podcasts.android.ui.player.PlayerScreen
import com.ramitsuri.podcasts.android.ui.player.PlayerViewModel
import com.ramitsuri.podcasts.android.ui.podcast.PodcastDetailsScreen
import com.ramitsuri.podcasts.android.ui.search.SearchScreen
import com.ramitsuri.podcasts.android.ui.settings.SettingsScreen
import com.ramitsuri.podcasts.android.ui.subscriptions.SubscriptionsScreen
import com.ramitsuri.podcasts.viewmodel.DownloadsViewModel
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.EpisodeHistoryViewModel
import com.ramitsuri.podcasts.viewmodel.FavoritesViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
import com.ramitsuri.podcasts.viewmodel.PodcastDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.QueueViewModel
import com.ramitsuri.podcasts.viewmodel.SearchViewModel
import com.ramitsuri.podcasts.viewmodel.SettingsViewModel
import com.ramitsuri.podcasts.viewmodel.SubscriptionsViewModel
import kotlinx.coroutines.launch
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
    val isExpanded by remember(scaffoldSheetState.bottomSheetState) {
        derivedStateOf {
            val currentValue = scaffoldSheetState.bottomSheetState.currentValue
            val targetValue = scaffoldSheetState.bottomSheetState.targetValue

            if (currentValue == targetValue) {
                currentValue == SheetValue.Expanded
            } else if (currentValue == SheetValue.Expanded && targetValue == SheetValue.PartiallyExpanded) {
                false
            } else {
                true
            }
        }
    }
    var navBarHeight by remember { mutableIntStateOf(0) }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier =
            Modifier
                .fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = !isExpanded,
                enter = slideInVertically { navBarHeight },
                exit = slideOutVertically { navBarHeight },
            ) {
                BottomNavBar(
                    modifier =
                        Modifier
                            .onGloballyPositioned {
                                navBarHeight = it.size.height
                            },
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
        val roundedCornerDp by animateDpAsState(
            targetValue = if (isExpanded) 16.dp else 0.dp,
            label = "rounded_corner_animation",
        )
        val coroutineScope = rememberCoroutineScope()
        BottomSheetScaffold(
            scaffoldState = scaffoldSheetState,
            sheetPeekHeight = bottomPadding,
            modifier = Modifier.padding(if (bottomSheetVisible) innerPadding else PaddingValues(bottom = 0.dp)),
            sheetDragHandle = { },
            sheetShape = RoundedCornerShape(topStart = roundedCornerDp, topEnd = roundedCornerDp),
            sheetContent =
                if (bottomSheetVisible) {
                    {
                        LifecycleStartEffect(LocalLifecycleOwner.current) {
                            playerViewModel.initializePlayer()

                            onStopOrDispose {
                                playerViewModel.releasePlayer()
                            }
                        }
                        PlayerScreen(
                            isExpanded = isExpanded,
                            state = playerState,
                            onNotExpandedHeightKnown = {
                                peekHeightPx = it
                            },
                            onEpisodeTitleClicked = {
                                coroutineScope.launch {
                                    scaffoldSheetState.bottomSheetState.partialExpand()
                                }
                                val episodeId = playerState.episodeId
                                if (episodeId != null) {
                                    navController.navigate(Route.episodeDetails(episodeId))
                                }
                            },
                            onPodcastNameClicked = {
                                coroutineScope.launch {
                                    scaffoldSheetState.bottomSheetState.partialExpand()
                                }
                                val id = playerState.podcastId
                                if (id != null) {
                                    navController.navigate(
                                        Route.podcastDetails(
                                            podcastId = id,
                                            refreshPodcast = false,
                                        ),
                                    )
                                }
                            },
                            onGoToQueueClicked = {
                                coroutineScope.launch {
                                    scaffoldSheetState.bottomSheetState.partialExpand()
                                }
                                navController.navigate(Route.QUEUE.value)
                            },
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
                            onNotExpandedPlayerClicked = {
                                coroutineScope.launch {
                                    scaffoldSheetState.bottomSheetState.expand()
                                }
                            },
                            onFavoriteClicked = playerViewModel::onFavoriteClicked,
                            onNotFavoriteClicked = playerViewModel::onNotFavoriteClicked,
                        )
                    }
                } else {
                    { }
                },
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.HOME.route.value,
                modifier =
                    modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom =
                                if (bottomSheetVisible) {
                                    with(LocalDensity.current) {
                                        peekHeightPx.toDp()
                                    }
                                } else {
                                    innerPadding.calculateBottomPadding()
                                },
                        ),
            ) {
                composable(route = BottomNavItem.HOME.route.value) {
                    val viewModel = koinViewModel<HomeViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    HomeScreen(
                        state = state,
                        onSettingsClicked = {
                            navController.navigate(Route.SETTINGS.value)
                        },
                        onImportSubscriptionsClicked = {
                            navController.navigate(Route.IMPORT_SUBSCRIPTIONS.value)
                        },
                        onPodcastClicked = {
                            navController.navigate(
                                Route.podcastDetails(
                                    podcastId = it,
                                    refreshPodcast = false,
                                ),
                            )
                        },
                        onPodcastHasNewSeen = viewModel::markPodcastHasNewSeen,
                        onMorePodcastsClicked = {
                            navController.navigate(Route.SUBSCRIPTIONS.value)
                        },
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
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
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        onNextPageRequested = viewModel::onNextPageRequested,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(route = BottomNavItem.EXPLORE.route.value) {
                    val viewModel = koinViewModel<SearchViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    SearchScreen(
                        state = state,
                        onSettingsClicked = {
                            navController.navigate(Route.SETTINGS.value)
                        },
                        onPodcastClicked = {
                            navController.navigate(
                                Route.podcastDetails(
                                    podcastId = it,
                                    refreshPodcast = true,
                                ),
                            )
                        },
                        onSearchTermUpdated = viewModel::onSearchTermUpdated,
                        onSearchRequested = viewModel::search,
                        onSearchCleared = viewModel::clearSearch,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(route = BottomNavItem.LIBRARY.route.value) {
                    LibraryScreen(
                        onSettingsClicked = {
                            navController.navigate(Route.SETTINGS.value)
                        },
                        onSubscriptionsClicked = { navController.navigate(Route.SUBSCRIPTIONS.value) },
                        onQueueClicked = { navController.navigate(Route.QUEUE.value) },
                        onDownloadsClicked = { navController.navigate(Route.DOWNLOADS.value) },
                        onHistoryClicked = { navController.navigate(Route.EPISODE_HISTORY.value) },
                        onFavoritesClicked = { navController.navigate(Route.FAVORITES.value) },
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.IMPORT_SUBSCRIPTIONS.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
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
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.EPISODE_DETAILS.routeWithArgName(),
                    arguments = Route.EPISODE_DETAILS.navArgs(),
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
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
                        onPodcastNameClicked = { podcastId ->
                            navController.navigate(
                                Route.podcastDetails(
                                    podcastId = podcastId,
                                    refreshPodcast = false,
                                ),
                            )
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
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.PODCAST_DETAILS.routeWithArgName(),
                    arguments = Route.PODCAST_DETAILS.navArgs(),
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) { backStackEntry ->
                    val podcastId = backStackEntry.arguments?.getLong(RouteArgs.PODCAST_ID.value)
                    val refreshPodcast = backStackEntry.arguments?.getBoolean(RouteArgs.REFRESH_PODCAST.value)
                    val viewModel =
                        koinViewModel<PodcastDetailsViewModel>(
                            parameters = { parametersOf(refreshPodcast, podcastId) },
                        )
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    PodcastDetailsScreen(
                        state = state,
                        onBack = { navController.navigateUp() },
                        onSubscribeClicked = viewModel::onSubscribeClicked,
                        onUnsubscribeClicked = viewModel::onUnsubscribeClicked,
                        toggleAutoDownloadClicked = viewModel::toggleAutoDownloadClicked,
                        toggleAutoAddToQueueClicked = viewModel::toggleAutoAddToQueueClicked,
                        toggleShowCompletedEpisodesClicked = viewModel::toggleShowCompletedEpisodes,
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
                        },
                        onEpisodeSelectionChanged = viewModel::onEpisodeSelectionChanged,
                        onEpisodePlayClicked = viewModel::onEpisodePlayClicked,
                        onEpisodePauseClicked = viewModel::onEpisodePauseClicked,
                        onEpisodeAddToQueueClicked = viewModel::onEpisodeAddToQueueClicked,
                        onEpisodeRemoveFromQueueClicked = viewModel::onEpisodeRemoveFromQueueClicked,
                        onEpisodeDownloadClicked = viewModel::onEpisodeDownloadClicked,
                        onEpisodeRemoveDownloadClicked = viewModel::onEpisodeRemoveDownloadClicked,
                        onEpisodeCancelDownloadClicked = viewModel::onEpisodeCancelDownloadClicked,
                        onEpisodePlayedClicked = viewModel::onEpisodePlayedClicked,
                        onEpisodeNotPlayedClicked = viewModel::onEpisodeNotPlayedClicked,
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        onEpisodeSortOrderClicked = viewModel::onSortOrderClicked,
                        onSelectAllEpisodesClicked = viewModel::onSelectAllEpisodes,
                        onUnselectAllEpisodesClicked = viewModel::onUnselectAllEpisodes,
                        onMarkSelectedEpisodesAsPlayed = viewModel::onMarkSelectedAsPlayed,
                        onMarkSelectedEpisodesAsNotPlayed = viewModel::onMarkSelectedAsNotPlayed,
                        onNextPageRequested = viewModel::onNextPageRequested,
                        onLoadOlderEpisodesRequested = viewModel::onLoadOlderEpisodesRequested,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.QUEUE.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<QueueViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    QueueScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEpisodesRearranged = viewModel::onEpisodeRearrangementRequested,
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
                        },
                        onEpisodePlayClicked = viewModel::onEpisodePlayClicked,
                        onEpisodePauseClicked = viewModel::onEpisodePauseClicked,
                        onEpisodeRemoveFromQueueClicked = viewModel::onEpisodeRemoveFromQueueClicked,
                        onEpisodeDownloadClicked = viewModel::onEpisodeDownloadClicked,
                        onEpisodeRemoveDownloadClicked = viewModel::onEpisodeRemoveDownloadClicked,
                        onEpisodeCancelDownloadClicked = viewModel::onEpisodeCancelDownloadClicked,
                        onEpisodePlayedClicked = viewModel::onEpisodePlayedClicked,
                        onEpisodeNotPlayedClicked = viewModel::onEpisodeNotPlayedClicked,
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.SUBSCRIPTIONS.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<SubscriptionsViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    SubscriptionsScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onPodcastClicked = {
                            navController.navigate(
                                Route.podcastDetails(
                                    podcastId = it,
                                    refreshPodcast = false,
                                ),
                            )
                        },
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.DOWNLOADS.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<DownloadsViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    DownloadsScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
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
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.FAVORITES.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<FavoritesViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    FavoritesScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
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
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.EPISODE_HISTORY.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<EpisodeHistoryViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    EpisodeHistoryScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEpisodeClicked = {
                            navController.navigate(Route.episodeDetails(episodeId = it))
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
                        onEpisodeFavoriteClicked = viewModel::onEpisodeMarkFavorite,
                        onEpisodeNotFavoriteClicked = viewModel::onEpisodeMarkNotFavorite,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.SETTINGS.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    SettingsScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        toggleAutoPlayNextInQueue = viewModel::toggleAutoPlayNextInQueue,
                        onFetchRequested = viewModel::fetch,
                        onRemoveCompletedAfterSelected = viewModel::setRemoveCompletedAfter,
                        onRemoveUnfinishedAfterSelected = viewModel::setRemoveUnfinishedAfter,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        tween(300),
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        tween(300),
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        tween(300),
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        tween(300),
    )

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
