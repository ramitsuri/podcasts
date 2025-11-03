package com.ramitsuri.podcasts

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.repositories.TrendingPodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.AndroidForegroundStateObserver
import com.ramitsuri.podcasts.utils.AndroidRemoteConfigHelper
import com.ramitsuri.podcasts.utils.CategoryHelper
import com.ramitsuri.podcasts.utils.DispatcherProvider
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.ForegroundStateObserver
import com.ramitsuri.podcasts.utils.LanguageHelper
import com.ramitsuri.podcasts.utils.RemoteConfigHelper
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
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import okio.Path
import okio.Path.Companion.toPath
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

actual val platformModule =
    module {
        single<HttpClientEngine> {
            Android.create()
        }

        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = PodcastsDatabase.Schema,
                context = get<Application>(),
                name = "podcasts.db",
            )
        }

        single<DispatcherProvider> {
            DispatcherProvider()
        }

        single<ForegroundStateObserver> {
            AndroidForegroundStateObserver()
        }

        single<RemoteConfigHelper> {
            AndroidRemoteConfigHelper(get<Settings>())
        }

        viewModel<HomeViewModel> {
            HomeViewModel(
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
                podcastsRepository = get<PodcastsRepository>(),
                sessionHistoryRepository = get<SessionHistoryRepository>(),
                clock = get<Clock>(),
                episodeFetcher = get<EpisodeFetcher>(),
            )
        }

        viewModel<QueueViewModel> {
            QueueViewModel(
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
                playerController = get<PlayerController>(),
            )
        }

        viewModel<EpisodeDetailsViewModel> { parameters ->
            EpisodeDetailsViewModel(
                episodeId = parameters.get(),
                repository = get<EpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                settings = get<Settings>(),
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
                podcastId = parameters.get(),
                remoteConfigHelper = get<RemoteConfigHelper>(),
            )
        }

        viewModel<SearchViewModel> {
            SearchViewModel(
                podcastsRepository = get<PodcastsRepository>(),
            )
        }

        viewModel<ExploreViewModel> {
            ExploreViewModel(
                trendingPodcastsRepository = get<TrendingPodcastsRepository>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
                clock = get<Clock>(),
                languageHelper = get<LanguageHelper>(),
                categoryHelper = get<CategoryHelper>(),
            )
        }

        viewModel<PodcastDetailsViewModel> { parameters ->
            PodcastDetailsViewModel(
                shouldRefreshPodcast = parameters.get(),
                podcastId = parameters.get(),
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
                episodesRepository = get<EpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                settings = get<Settings>(),
                repository = get<PodcastsRepository>(),
                longLivingScope = get<CoroutineScope>(),
            )
        }

        viewModel<SubscriptionsViewModel> {
            SubscriptionsViewModel(
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
            )
        }

        viewModel<DownloadsViewModel> {
            DownloadsViewModel(
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
            )
        }

        viewModel<FavoritesViewModel> {
            FavoritesViewModel(
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
            )
        }

        viewModel<EpisodeHistoryViewModel> {
            EpisodeHistoryViewModel(
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                repository = get<SessionHistoryRepository>(),
                settings = get<Settings>(),
                timeZone = get<TimeZone>(),
            )
        }

        viewModel<SettingsViewModel> {
            SettingsViewModel(
                settings = get<Settings>(),
                episodeFetcher = get<EpisodeFetcher>(),
                longLivingScope = get<CoroutineScope>(),
                clock = get<Clock>(),
            )
        }

        viewModel<YearEndReviewViewModel> {
            YearEndReviewViewModel(
                podcastsRepository = get<PodcastsRepository>(),
                sessionHistoryRepository = get<SessionHistoryRepository>(),
                timeZone = get<TimeZone>(),
            )
        }

        viewModel<LibraryViewModel> {
            LibraryViewModel(
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
            )
        }

        factory<Path> {
            val dataStoreFileName = get<String>(qualifier = KoinQualifier.DATA_STORE_FILE_NAME)
            get<Application>().filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
    }
