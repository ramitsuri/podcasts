package com.ramitsuri.podcasts

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.DispatcherProvider
import com.ramitsuri.podcasts.viewmodel.EpisodeDetailsViewModel
import com.ramitsuri.podcasts.viewmodel.HomeViewModel
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
                episodesRepository = get<EpisodesRepository>(),
                settings = get<Settings>(),
                longLivingScope = get<CoroutineScope>(),
            )
        }

        viewModel<EpisodeDetailsViewModel> { parameters ->
            EpisodeDetailsViewModel(
                episodeId = parameters.get(),
                repository = get<EpisodesRepository>(),
            )
        }

        factory<Path> {
            val dataStoreFileName = get<String>(qualifier = KoinQualifier.DATA_STORE_FILE_NAME)
            get<Application>().filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
    }
