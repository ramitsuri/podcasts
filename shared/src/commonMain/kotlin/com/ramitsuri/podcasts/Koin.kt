package com.ramitsuri.podcasts

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
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import com.ramitsuri.podcasts.utils.DispatcherProvider
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule,
            platformModule,
            coreModule,
        )
    }
    return koinApplication
}

private val coreModule = module {

    single<HttpClient> {
        provideHttpClient(
            isDebug = get(),
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

    single<PodcastsDao> {
        PodcastsDaoImpl(
            podcastEntityQueries = get<PodcastsDatabase>().podcastEntityQueries,
            podcastAdditionalInfoEntityQueries = get<PodcastsDatabase>().podcastAdditionalInfoEntityQueries,
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }

    single<Clock> {
        Clock.System
    }

    factory<PodcastsApi> {
        PodcastsApiImpl(
            baseUrl = get(),
            httpClient = get(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }

    factory<CategoriesApi> {
        CategoriesApiImpl(
            baseUrl = get(),
            httpClient = get(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }

    factory<EpisodesApi> {
        EpisodesApiImpl(
            baseUrl = get(),
            httpClient = get(),
            ioDispatcher = get<DispatcherProvider>().io,
        )
    }

    factory<String> {
        "https://api.podcastindex.org/api/1.0"
    }
}

expect val platformModule: Module
