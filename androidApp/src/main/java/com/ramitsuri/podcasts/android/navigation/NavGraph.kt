package com.ramitsuri.podcasts.android.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.rememberBottomSheetState
import com.ramitsuri.podcasts.android.ui.backuprestore.BackupRestoreScreen
import com.ramitsuri.podcasts.android.ui.backuprestore.BackupRestoreViewModel
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
import com.ramitsuri.podcasts.android.ui.review.YearEndReviewScreen
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
import com.ramitsuri.podcasts.viewmodel.YearEndReviewViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    var bottomPadding by remember { mutableStateOf(0.dp) }
    val bottomSheetDetentPeek =
        SheetDetent(identifier = "peek") { _, _ ->
            bottomPadding
        }

    val bottomSheetState =
        rememberBottomSheetState(
            initialDetent = bottomSheetDetentPeek,
            detents = listOf(bottomSheetDetentPeek, FullyExpanded),
        )
    val sheetExpandProgress =
        if (bottomSheetState.isIdle && bottomSheetState.currentDetent == bottomSheetDetentPeek) {
            0f
        } else {
            bottomSheetState.progress
        }
    var canShowBottomNav by remember(bottomSheetState) { mutableStateOf(true) }
    var canShowPlayer by remember(bottomSheetState) { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    fun expandOrCollapsePlayer(expand: Boolean) {
        coroutineScope.launch {
            if (expand) {
                bottomSheetState.animateTo(FullyExpanded)
            } else {
                bottomSheetState.animateTo(bottomSheetDetentPeek)
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
                visible = canShowBottomNav,
                enter = slideInVertically { navBarHeight },
                exit = slideOutVertically { navBarHeight },
            ) {
                BottomNavBar(
                    modifier =
                        Modifier
                            .onGloballyPositioned {
                                navBarHeight = it.size.height
                            }.offset { IntOffset(x = 0, navBarHeight.times(sheetExpandProgress).toInt()) },
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
        val bottomSheetVisible = playerState.hasEverBeenPlayed && canShowPlayer
        bottomPadding =
            if (bottomSheetVisible) {
                with(LocalDensity.current) {
                    innerPadding.calculateBottomPadding() + peekHeightPx.toDp()
                }
            } else {
                0.dp
            }
        Box(modifier = modifier.fillMaxSize()) {
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
                                navOptions { popUpTo(BottomNavItem.HOME.route.value) },
                            )
                        },
                        onPodcastHasNewSeen = viewModel::markPodcastHasNewSeen,
                        onMorePodcastsClicked = {
                            navController.navigate(
                                Route.SUBSCRIPTIONS.value,
                                navOptions { popUpTo(BottomNavItem.HOME.route.value) },
                            )
                        },
                        onEpisodeClicked = {
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions { popUpTo(BottomNavItem.HOME.route.value) },
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
                        onNextPageRequested = viewModel::onNextPageRequested,
                        onYearEndReviewClicked = {
                            canShowPlayer = false
                            canShowBottomNav = false
                            navController.navigate(Route.YEAR_END_REVIEW.value)
                        },
                        onRefresh = viewModel::onRefresh,
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
                            navController.navigate(
                                Route.SETTINGS.value,
                                navOptions { popUpTo(BottomNavItem.EXPLORE.route.value) },
                            )
                        },
                        onPodcastClicked = {
                            navController.navigate(
                                Route.podcastDetails(
                                    podcastId = it,
                                    refreshPodcast = true,
                                ),
                                navOptions { popUpTo(BottomNavItem.EXPLORE.route.value) },
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
                            navController.navigate(
                                Route.SETTINGS.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
                        onSubscriptionsClicked = {
                            navController.navigate(
                                Route.SUBSCRIPTIONS.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
                        onQueueClicked = {
                            navController.navigate(
                                Route.QUEUE.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
                        onDownloadsClicked = {
                            navController.navigate(
                                Route.DOWNLOADS.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
                        onHistoryClicked = {
                            navController.navigate(
                                Route.EPISODE_HISTORY.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
                        onFavoritesClicked = {
                            navController.navigate(
                                Route.FAVORITES.value,
                                navOptions { popUpTo(BottomNavItem.LIBRARY.route.value) },
                            )
                        },
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
                    deepLinks =
                        listOf(
                            navDeepLink {
                                uriPattern = Route.EPISODE_DETAILS.deepLinkWithArgName()
                            },
                        ),
                ) { backStackEntry ->
                    val viewModel =
                        backStackEntry.arguments.let { arg ->
                            val episodeId = arg?.getString(RouteArgs.EPISODE_ID.value)?.let { Uri.decode(it) }
                            val podcastId = arg?.getString(RouteArgs.PODCAST_ID.value)?.toLongOrNull()
                            koinViewModel<EpisodeDetailsViewModel>(
                                parameters = { parametersOf(episodeId, podcastId) },
                            )
                        }
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
                                navOptions {
                                    popUpTo(
                                        Route.EPISODE_DETAILS.routeWithArgName(),
                                        popUpToBuilder = { inclusive = true },
                                    )
                                },
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
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions {
                                    popUpTo(
                                        Route.PODCAST_DETAILS.routeWithArgName(),
                                        popUpToBuilder = { inclusive = true },
                                    )
                                },
                            )
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
                        onSearchTermUpdated = viewModel::onSearchTermUpdated,
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
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions { popUpTo(Route.QUEUE.value) },
                            )
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
                                navOptions { popUpTo(Route.SUBSCRIPTIONS.value) },
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
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions { popUpTo(Route.DOWNLOADS.value) },
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
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions { popUpTo(Route.FAVORITES.value) },
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
                            navController.navigate(
                                Route.episodeDetails(episodeId = it),
                                navOptions { popUpTo(Route.EPISODE_HISTORY.value) },
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
                        onVersionClicked = viewModel::onVersionClicked,
                        onBackupRestoreClicked = {
                            canShowBottomNav = false
                            canShowPlayer = false
                            navController.navigate(Route.BACKUP_RESTORE.value)
                        },
                        toggleShouldDownloadOnWifiOnly = viewModel::toggleShouldDownloadOnWifiOnly,
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding(),
                    )
                }

                composable(
                    route = Route.YEAR_END_REVIEW.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewmodel = koinViewModel<YearEndReviewViewModel>()
                    val state by viewmodel.state.collectAsStateWithLifecycle()

                    YearEndReviewScreen(
                        state = state,
                        onBack = {
                            canShowPlayer = true
                            canShowBottomNav = true
                            navController.popBackStack()
                        },
                        onNextPage = viewmodel::onNextPage,
                        onPreviousPage = viewmodel::onPreviousPage,
                    )
                }

                composable(
                    route = Route.BACKUP_RESTORE.value,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                ) {
                    val viewmodel =
                        viewModel<BackupRestoreViewModel>(
                            factory = BackupRestoreViewModel.factory(),
                        )
                    val state by viewmodel.state.collectAsStateWithLifecycle()

                    BackupRestoreScreen(
                        state = state,
                        onRestoreFilePicked = viewmodel::onRestoreFilePicked,
                        onBackupFilePicked = viewmodel::onBackupFilePicked,
                        onBack = {
                            canShowBottomNav = true
                            canShowPlayer = true
                            navController.popBackStack()
                        },
                    )
                }
            }

            if (sheetExpandProgress != 0f) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f.times(sheetExpandProgress)))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { expandOrCollapsePlayer(expand = false) },
                            ),
                )
            }
            val cornerDp = 24.dp.times(sheetExpandProgress)
            BottomSheet(
                state = bottomSheetState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = cornerDp, topEnd = cornerDp)),
            ) {
                if (bottomSheetVisible) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        LifecycleStartEffect(Unit, LocalLifecycleOwner.current) {
                            playerViewModel.viewStarted()

                            onStopOrDispose {
                                playerViewModel.viewStopped()
                            }
                        }
                        PlayerScreen(
                            expandProgress = sheetExpandProgress,
                            state = playerState,
                            onNotExpandedHeightKnown = {
                                peekHeightPx = it
                            },
                            onEpisodeTitleClicked = {
                                expandOrCollapsePlayer(expand = false)
                                val episodeId = playerState.episodeId
                                if (episodeId != null) {
                                    navController.navigate(
                                        Route.episodeDetails(episodeId),
                                        navOptions { popUpTo(BottomNavItem.HOME.route.value) },
                                    )
                                }
                            },
                            onPodcastNameClicked = {
                                expandOrCollapsePlayer(expand = false)
                                val id = playerState.podcastId
                                if (id != null) {
                                    navController.navigate(
                                        Route.podcastDetails(
                                            podcastId = id,
                                            refreshPodcast = false,
                                        ),
                                        navOptions { popUpTo(BottomNavItem.HOME.route.value) },
                                    )
                                }
                            },
                            onGoToQueueClicked = {
                                expandOrCollapsePlayer(expand = false)
                                navController.navigate(
                                    Route.QUEUE.value,
                                    navOptions { popUpTo(BottomNavItem.HOME.route.value) },
                                )
                            },
                            onReplayClicked = playerViewModel::onReplayRequested,
                            onPauseClicked = playerViewModel::onPauseClicked,
                            onPlayClicked = playerViewModel::onPlayClicked,
                            onSkipClicked = playerViewModel::onSkipRequested,
                            onSeekValueChange = playerViewModel::onSeekRequested,
                            onPlaybackSpeedSet = playerViewModel::onSpeedChangeRequested,
                            onToggleTrimSilence = playerViewModel::toggleTrimSilence,
                            onTimerDecrement = playerViewModel::onSleepTimerDecreaseRequested,
                            onTimerIncrement = playerViewModel::onSleepTimerIncreaseRequested,
                            onTimerCanceled = playerViewModel::onSleepTimerCancelRequested,
                            onEndOfEpisodeTimerSet = playerViewModel::onSleepTimerEndOfEpisodeRequested,
                            onCustomTimerSet = playerViewModel::onSleepTimerCustomRequested,
                            onNotExpandedPlayerClicked = {
                                expandOrCollapsePlayer(expand = true)
                            },
                            onFavoriteClicked = playerViewModel::onFavoriteClicked,
                            onNotFavoriteClicked = playerViewModel::onNotFavoriteClicked,
                        )
                    }
                }
            }
            BackHandler(sheetExpandProgress == 1f) {
                expandOrCollapsePlayer(false)
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
