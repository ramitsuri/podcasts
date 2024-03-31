package com.ramitsuri.podcasts

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.DispatcherProvider
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
import com.ramitsuri.podcasts.viewmodel.PodcastDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.QueueViewModel
import com.ramitsuri.podcasts.viewmodel.SearchViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineScope
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

        viewModel<HomeViewModel> {
            HomeViewModel(
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
            )
        }

        viewModel<QueueViewModel> {
            QueueViewModel(
                episodeController = get<EpisodeController>(),
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
            )
        }

        viewModel<EpisodeDetailsViewModel> { parameters ->
            EpisodeDetailsViewModel(
                episodeId = parameters.get(),
                repository = get<EpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                settings = get<Settings>(),
            )
        }

        viewModel<SearchViewModel> {
            SearchViewModel(
                podcastsRepository = get<PodcastsRepository>(),
            )
        }

        viewModel<PodcastDetailsViewModel> { parameters ->
            PodcastDetailsViewModel(
                podcastId = parameters.get(),
                podcastsAndEpisodesRepository = get<PodcastsAndEpisodesRepository>(),
                episodesRepository = get<EpisodesRepository>(),
                episodeController = get<EpisodeController>(),
                settings = get<Settings>(),
                repository = get<PodcastsRepository>(),
                longLivingScope = get<CoroutineScope>(),
            )
        }

        factory<Path> {
            val dataStoreFileName = get<String>(qualifier = KoinQualifier.DATA_STORE_FILE_NAME)
            get<Application>().filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
    }
