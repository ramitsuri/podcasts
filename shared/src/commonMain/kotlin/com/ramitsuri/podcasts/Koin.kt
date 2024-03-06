package com.ramitsuri.podcasts

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ramitsuri.podcasts.database.dao.CategoryDaoImpl
import com.ramitsuri.podcasts.database.dao.EpisodesDaoImpl
import com.ramitsuri.podcasts.database.dao.PodcastsDaoImpl
import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.database.provideDatabase
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
import com.ramitsuri.podcasts.settings.DataStoreKeyValueStore
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.DispatcherProvider
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock
import okio.Path
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    val koinApplication =
        startKoin {
            modules(
                appModule,
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
                ioDispatcher = get<DispatcherProvider>().io,
            )
        }

        single<EpisodesRepository> {
            EpisodesRepository(
                episodesDao = get(),
                episodesApi = get(),
            )
        }

        single<PodcastsAndEpisodesRepository> {
            PodcastsAndEpisodesRepository(
                podcastsRepository = get(),
                episodesRepository = get(),
                ioDispatcher = get<DispatcherProvider>().io,
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

        single<Clock> {
            Clock.System
        }

        single<Settings> {
            val dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { get<Path>() })
            val keyValueStore = DataStoreKeyValueStore(dataStore)
            Settings(keyValueStore)
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
