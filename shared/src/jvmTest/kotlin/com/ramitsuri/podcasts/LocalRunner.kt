package com.ramitsuri.podcasts

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.api.interfaces.EpisodesApi
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.GetEpisodesRequest
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import java.time.Instant
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
            println(Instant.ofEpochSecond(items?.minBy { it.datePublished }?.datePublished ?: 0))
        }

    @BeforeTest
    fun before() {
        initKoin(
            appModule =
                module {
                    single<AppInfo> {
                        object : AppInfo {
                            override val isDebug = false
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
