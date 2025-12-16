package com.ramitsuri.podcasts.android.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.rememberBottomSheetState
import com.ramitsuri.podcasts.android.ui.backuprestore.BackupRestoreScreen
import com.ramitsuri.podcasts.android.ui.backuprestore.BackupRestoreViewModel
import com.ramitsuri.podcasts.android.ui.components.ScreenEventListener
import com.ramitsuri.podcasts.android.ui.downloads.DownloadsScreen
import com.ramitsuri.podcasts.android.ui.episode.EpisodeDetailsScreen
import com.ramitsuri.podcasts.android.ui.explore.ExploreScreen
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
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.Route.BackupRestore
import com.ramitsuri.podcasts.navigation.Route.Downloads
import com.ramitsuri.podcasts.navigation.Route.EpisodeDetails
import com.ramitsuri.podcasts.navigation.Route.EpisodeHistory
import com.ramitsuri.podcasts.navigation.Route.Explore
import com.ramitsuri.podcasts.navigation.Route.Favorites
import com.ramitsuri.podcasts.navigation.Route.Home
import com.ramitsuri.podcasts.navigation.Route.ImportSubscriptions
import com.ramitsuri.podcasts.navigation.Route.Library
import com.ramitsuri.podcasts.navigation.Route.PodcastDetails
import com.ramitsuri.podcasts.navigation.Route.Queue
import com.ramitsuri.podcasts.navigation.Route.Search
import com.ramitsuri.podcasts.navigation.Route.Settings
import com.ramitsuri.podcasts.navigation.Route.Subscriptions
import com.ramitsuri.podcasts.navigation.Route.YearEndReview
import com.ramitsuri.podcasts.navigation.deepLinkWithArgValue
import com.ramitsuri.podcasts.viewmodel.DownloadsViewModel
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.EpisodeHistoryViewModel
import com.ramitsuri.podcasts.viewmodel.ExploreViewModel
import com.ramitsuri.podcasts.viewmodel.FavoritesViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
import com.ramitsuri.podcasts.viewmodel.LibraryViewModel
import com.ramitsuri.podcasts.viewmodel.PodcastDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.QueueViewModel
import com.ramitsuri.podcasts.viewmodel.SearchViewModel
import com.ramitsuri.podcasts.viewmodel.SettingsViewModel
import com.ramitsuri.podcasts.viewmodel.SubscriptionsViewModel
import com.ramitsuri.podcasts.viewmodel.YearEndReviewViewModel
import com.ramitsuri.podcasts.widget.AppWidget.Companion.addWidget
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navigator: Navigator,
) {
    val context = LocalContext.current
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

    var scrollToTop by remember { mutableStateOf(false) }

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
                            }
                            .offset { IntOffset(x = 0, navBarHeight.times(sheetExpandProgress).toInt()) },
                    selectedNavDestination = navigator.currentDestination,
                    onHomeTabClicked = {
                        if (!navigator.navigateToMainDestination(BottomNavItem.HOME)) {
                            scrollToTop = true
                        }
                    },
                    onExploreClicked = {
                        navigator.navigateToMainDestination(BottomNavItem.EXPLORE)
                    },
                    onLibraryClicked = {
                        navigator.navigateToMainDestination(BottomNavItem.LIBRARY)
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

        val entryProvider: (Route) -> NavEntry<Route> = entryProvider {
            entry<Home>(
                metadata = topLevelAnimationMetaData,
            ) {
                val viewModel = koinViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                HomeScreen(
                    state = state,
                    scrollToTop = scrollToTop,
                    onScrolledToTop = { scrollToTop = false },
                    onSettingsClicked = {
                        navigator.navigate(Settings)
                    },
                    onImportSubscriptionsClicked = {
                        navigator.navigate(ImportSubscriptions)
                    },
                    onPodcastClicked = { podcastId ->
                        navigator.navigate(
                            PodcastDetails(podcastId, false),
                        )
                    },
                    onPodcastHasNewSeen = viewModel::markPodcastHasNewSeen,
                    onMorePodcastsClicked = {
                        navigator.navigate(
                            Subscriptions,
                        )
                    },
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                        navigator.navigate(YearEndReview)
                    },
                    onRefresh = viewModel::onRefresh,
                    modifier =
                        Modifier
                            .screen(),
                )
            }

            entry<Explore>(
                metadata = topLevelAnimationMetaData,
            ) {
                val viewModel = koinViewModel<ExploreViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                ExploreScreen(
                    state = state,
                    onSettingsClicked = { navigator.navigate(Settings) },
                    onPodcastClicked = { podcastId ->
                        navigator.navigate(
                            PodcastDetails(podcastId, true),
                        )
                    },
                    onSearchClicked = { navigator.navigate(Search) },
                    onLanguageClicked = viewModel::onLanguageClicked,
                    onCategoryClicked = viewModel::onCategoryClicked,
                    onRefresh = viewModel::onRefresh,
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<Library>(
                metadata = topLevelAnimationMetaData,
            ) {
                val viewModel = koinViewModel<LibraryViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LibraryScreen(
                    state = state,
                    onSettingsClicked = { navigator.navigate(Settings) },
                    onSubscriptionsClicked = { navigator.navigate(Subscriptions) },
                    onQueueClicked = { navigator.navigate(Queue) },
                    onDownloadsClicked = { navigator.navigate(Downloads) },
                    onHistoryClicked = { navigator.navigate(EpisodeHistory) },
                    onFavoritesClicked = { navigator.navigate(Favorites) },
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<Search> {
                val viewModel = koinViewModel<SearchViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                SearchScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onPodcastClicked = { podcastId ->
                        navigator.navigate(
                            PodcastDetails(podcastId, true),
                        )
                    },
                    onSearchTermUpdated = viewModel::onSearchTermUpdated,
                    onSearchRequested = viewModel::search,
                    onSearchCleared = viewModel::clearSearch,
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<ImportSubscriptions> {
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
                    onBack = { navigator.goBack() },
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<EpisodeDetails> { args ->
                val viewModel =
                    koinViewModel<EpisodeDetailsViewModel>(
                        parameters = { parametersOf(args.episodeId, args.podcastId) },
                    )
                val state by viewModel.state.collectAsStateWithLifecycle()
                EpisodeDetailsScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onPodcastNameClicked = { podcastId ->
                        navigator.navigate(
                            PodcastDetails(podcastId, false),
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
                            .screen()
                )
            }

            entry<PodcastDetails> { args ->
                val viewModel =
                    koinViewModel<PodcastDetailsViewModel>(
                        parameters = { parametersOf(args.refreshPodcast, args.podcastId) },
                    )
                val state by viewModel.state.collectAsStateWithLifecycle()

                PodcastDetailsScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onSubscribeClicked = viewModel::onSubscribeClicked,
                    onUnsubscribeClicked = viewModel::onUnsubscribeClicked,
                    toggleAutoDownloadClicked = viewModel::toggleAutoDownloadClicked,
                    toggleAutoAddToQueueClicked = viewModel::toggleAutoAddToQueueClicked,
                    toggleShowCompletedEpisodesClicked = viewModel::toggleShowCompletedEpisodes,
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                            .screen()
                )
            }

            entry<Queue> {
                val viewModel = koinViewModel<QueueViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                QueueScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onEpisodesRearranged = viewModel::onEpisodeRearrangementRequested,
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                    onEpisodesSortRequested = viewModel::onEpisodesSortRequested,
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<Subscriptions> {
                val viewModel = koinViewModel<SubscriptionsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                SubscriptionsScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onPodcastClicked = { podcastId ->
                        navigator.navigate(
                            PodcastDetails(podcastId, false),
                        )
                    },
                        modifier =
                            Modifier
                                .screen()
                )
            }

            entry<Downloads> {
                val viewModel = koinViewModel<DownloadsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                DownloadsScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                            .screen()
                )
            }

            entry<Favorites> {
                val viewModel = koinViewModel<FavoritesViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                FavoritesScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                            .screen()
                )
            }

            entry<EpisodeHistory> {
                val viewModel = koinViewModel<EpisodeHistoryViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                EpisodeHistoryScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onEpisodeClicked = { episodeId, podcastId ->
                        navigator.navigate(
                            EpisodeDetails(episodeId, podcastId),
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
                            .screen()
                )
            }

            entry<Settings> {
                val viewModel = koinViewModel<SettingsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                SettingsScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    toggleAutoPlayNextInQueue = viewModel::toggleAutoPlayNextInQueue,
                    onFetchRequested = viewModel::fetch,
                    onRemoveCompletedAfterSelected = viewModel::setRemoveCompletedAfter,
                    onRemoveUnfinishedAfterSelected = viewModel::setRemoveUnfinishedAfter,
                    onVersionClicked = viewModel::onVersionClicked,
                    onBackupRestoreClicked = { navigator.navigate(BackupRestore) },
                    toggleShouldDownloadOnWifiOnly = viewModel::toggleShouldDownloadOnWifiOnly,
                    onAddWidgetClicked = {
                        coroutineScope.launch {
                            context.addWidget()
                            viewModel.onWidgetItemSeen()
                        }
                    },
                    onWidgetItemSeen = viewModel::onWidgetItemSeen,
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<YearEndReview> {
                ScreenEventListener(
                    onStart = {
                        canShowPlayer = false
                        canShowBottomNav = false
                    },
                    onStop = {
                        canShowPlayer = true
                        canShowBottomNav = true
                    },
                )
                val viewmodel = koinViewModel<YearEndReviewViewModel>()
                val state by viewmodel.state.collectAsStateWithLifecycle()

                YearEndReviewScreen(
                    state = state,
                    onBack = { navigator.goBack() },
                    onNextPage = viewmodel::onNextPage,
                    onPreviousPage = viewmodel::onPreviousPage,
                    modifier =
                        Modifier
                            .screen()
                )
            }

            entry<BackupRestore> {
                ScreenEventListener(
                    onStart = {
                        canShowPlayer = false
                        canShowBottomNav = false
                    },
                    onStop = {
                        canShowPlayer = true
                        canShowBottomNav = true
                    },
                )
                val viewmodel =
                    viewModel<BackupRestoreViewModel>(
                        factory = BackupRestoreViewModel.factory(),
                    )
                val state by viewmodel.state.collectAsStateWithLifecycle()

                BackupRestoreScreen(
                    state = state,
                    onRestoreFilePicked = viewmodel::onRestoreFilePicked,
                    onBackupFilePicked = viewmodel::onBackupFilePicked,
                    onBack = navigator::goBack,
                    modifier =
                        Modifier
                            .screen()
                )
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            NavDisplay(
                backStack = navigator.backstack,
                entryProvider = entryProvider,
                onBack = { navigator.goBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
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
            )

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
                                val podcastId = playerState.podcastId
                                if (episodeId != null && podcastId != null) {
                                    navigator.navigate(
                                        EpisodeDetails(episodeId, podcastId),
                                    )
                                }
                            },
                            onPodcastNameClicked = {
                                expandOrCollapsePlayer(expand = false)
                                val id = playerState.podcastId
                                if (id != null) {
                                    navigator.navigate(
                                        PodcastDetails(id, false),
                                    )
                                }
                            },
                            onGoToQueueClicked = {
                                expandOrCollapsePlayer(expand = false)
                                navigator.navigate(
                                    Queue,
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

@Composable
private fun BottomNavBar(
    selectedNavDestination: Route?,
    onHomeTabClicked: () -> Unit,
    onExploreClicked: () -> Unit,
    onLibraryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = selectedNavDestination == item.route
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

@Composable
private fun Modifier.screen() = background(MaterialTheme.colorScheme.background)
    .statusBarsPadding()
    .displayCutoutPadding()

private fun Navigator.navigateToMainDestination(to: BottomNavItem): Boolean {
    if (currentDestination == to.route) {
        return false
    }
    navigate(to.route)
    return true
}

private val topLevelAnimationMetaData: Map<String, Any>
    get() = NavDisplay.transitionSpec {
        EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
    } + NavDisplay.popTransitionSpec {
        EnterTransition.None togetherWith
            fadeOut(tween(300))
    } + NavDisplay.predictivePopTransitionSpec {
        EnterTransition.None togetherWith
            fadeOut(tween(300))
    }
