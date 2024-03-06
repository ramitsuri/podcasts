package com.ramitsuri.podcasts

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.utils.DispatcherProvider
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.datetime.Clock
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
                episodesRepository = get<EpisodesRepository>(),
                clock = get<Clock>()
            )
        }

        viewModel<EpisodeDetailsViewModel> { parameters ->
            EpisodeDetailsViewModel(
                episodeId = parameters.get(),
                repository = get<EpisodesRepository>(),
            )
        }
    }
