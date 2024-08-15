package com.ramitsuri.podcasts

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ramitsuri.podcasts.database.dao.CategoryDaoImpl
import com.ramitsuri.podcasts.database.dao.EpisodesDaoImpl
import com.ramitsuri.podcasts.database.dao.PodcastsDaoImpl
import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDao
import com.ramitsuri.podcasts.database.dao.interfaces.SessionActionDaoImpl
import com.ramitsuri.podcasts.database.provideDatabase
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.network.api.CategoriesApiImpl
import com.ramitsuri.podcasts.network.api.EpisodesApiImpl
import com.ramitsuri.podcasts.network.api.PodcastsApiImpl
import com.ramitsuri.podcasts.network.api.interfaces.CategoriesApi
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.provideHttpClient
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.repositories.SessionHistoryRepository
import com.ramitsuri.podcasts.settings.DataStoreKeyValueStore
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.DispatcherProvider
import com.ramitsuri.podcasts.utils.EpisodeController
import com.ramitsuri.podcasts.utils.EpisodeControllerImpl
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.ForegroundStateObserver
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import okio.Path
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun initKoin(appModule: KoinApplication.() -> Module): KoinApplication {
    val koinApplication =
        startKoin {
            modules(
                appModule(),
                platformModule,
                coreModule,
            )
        }
    return koinApplication
}

private val coreModule =
    module {

        single<HttpClient> {
            provideHttpClient(
                isDebug = get<AppInfo>().isDebug,
                clock = get(),
                clientEngine = get(),
            )
        }

        single<PodcastsDatabase> {
            provideDatabase(
                driver = get(),
            )
        }

        single<PodcastsRepository> {
            PodcastsRepository(
                podcastsApi = get(),
                podcastsDao = get(),
                categoryDao = get(),
            )
        }

        single<EpisodesRepository> {
            EpisodesRepository(
                episodesDao = get(),
                episodesApi = get(),
                settings = get(),
            )
        }

        single<PodcastsAndEpisodesRepository> {
            PodcastsAndEpisodesRepository(
                podcastsRepository = get(),
                episodesRepository = get(),
                sessionHistoryRepository = get(),
                ioDispatcher = get<DispatcherProvider>().io,
                episodeDownloader = get<EpisodeDownloader>(),
            )
        }

        single<SessionHistoryRepository> {
            SessionHistoryRepository(
                sessionActionDao = get(),
                episodesRepository = get(),
            )
        }

        single<PodcastsDao> {
            PodcastsDaoImpl(
                podcastEntityQueries = get<PodcastsDatabase>().podcastEntityQueries,
                podcastAdditionalInfoEntityQueries = get<PodcastsDatabase>().podcastAdditionalInfoEntityQueries,
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        single<CategoryDao> {
            CategoryDaoImpl(
                categoryEntityQueries = get<PodcastsDatabase>().categoryEntityQueries,
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        single<EpisodesDao> {
            EpisodesDaoImpl(
                episodeEntityQueries = get<PodcastsDatabase>().episodeEntityQueries,
                episodeAdditionalInfoEntityQueries = get<PodcastsDatabase>().episodeAdditionalInfoEntityQueries,
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        single<SessionActionDao> {
            SessionActionDaoImpl(
                sessionHistoryQueries = get<PodcastsDatabase>().sessionHistoryQueries,
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        single<Clock> {
            Clock.System
        }

        single<TimeZone> {
            TimeZone.currentSystemDefault()
        }

        single<Settings> {
            val dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { get<Path>() })
            val keyValueStore = DataStoreKeyValueStore(dataStore)
            Settings(keyValueStore)
        }

        single<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }

        single<EpisodeController> {
            EpisodeControllerImpl(
                longLivingScope = get(),
                episodesRepository = get(),
                playerController = get(),
                episodeDownloader = get(),
            )
        }

        single<EpisodeFetcher> {
            EpisodeFetcher(
                repository = get<PodcastsAndEpisodesRepository>(),
                settings = get<Settings>(),
                clock = get<Clock>(),
                foregroundStateObserver = get<ForegroundStateObserver>(),
                longLivingScope = get<CoroutineScope>(),
            )
        }

        factory<PodcastsApi> {
            PodcastsApiImpl(
                baseUrl = get(qualifier = KoinQualifier.BASE_API_URL),
                httpClient = get(),
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        factory<CategoriesApi> {
            CategoriesApiImpl(
                baseUrl = get(qualifier = KoinQualifier.BASE_API_URL),
                httpClient = get(),
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        factory<EpisodesApi> {
            EpisodesApiImpl(
                baseUrl = get(qualifier = KoinQualifier.BASE_API_URL),
                httpClient = get(),
                ioDispatcher = get<DispatcherProvider>().io,
                isDebug = get<AppInfo>().isDebug,
            )
        }

        factory<String>(qualifier = KoinQualifier.BASE_API_URL) {
            "https://api.podcastindex.org/api/1.0"
        }

        factory<String>(qualifier = KoinQualifier.DATA_STORE_FILE_NAME) {
            "podcasts.preferences_pb"
        }
    }

expect val platformModule: Module

internal object KoinQualifier {
    val BASE_API_URL = named("base_api_url")
    val DATA_STORE_FILE_NAME = named("data_store_file_name")
}
