package com.ramitsuri.podcasts

import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Class to just test functionality without invoking the android app
 */
internal class LocalRunner : KoinTest {
    @Test
    fun podcastsApiTest(): Unit =
        runBlocking {
            (
                get<PodcastsApi>().search(SearchPodcastsRequest("Joe Rogan"))
                    as? PodcastResult.Success
            )?.data?.podcasts?.forEach {
                println(it.id.toString() + " | " + it.title)
            }
        }

    @Test
    fun episodesApiTest(): Unit =
        runBlocking {
            val items =
                (
                    get<EpisodesApi>().getByPodcastId(
                        GetEpisodesRequest(
                            id = 1153200,
                            sinceEpochSeconds = 1614709499,
                        ),
                    ) as? PodcastResult.Success
                )?.data?.items
            println(Instant.fromEpochSeconds(items?.minBy { it.datePublished }?.datePublished ?: 0))
        }

    @Test
    fun databaseTest(): Unit =
        runBlocking {
            PodcastsDatabase.Schema.create(get<SqlDriver>())
        }

    @BeforeTest
    fun before() {
        initKoin(
            appModule =
                module {
                    single<AppInfo> {
                        object : AppInfo {
                            override val isDebug = true
                        }
                    }
                },
        )
    }

    @AfterTest
    fun after() {
        stopKoin()
    }
}
