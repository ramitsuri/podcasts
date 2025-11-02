package com.ramitsuri.podcasts

import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.utils.Logger
import com.ramitsuri.podcasts.utils.TestClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class BaseTest(
    private val initDatabase: Boolean = true,
    private val timeZone: TimeZone = TimeZone.of("America/Los_Angeles"),
) : KoinTest {
    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun tearDown() {
        stopKoin()
        if (Path("test.db").deleteIfExists()) {
            println("deleted database file")
        }
        Paths.get(TEMP_DIR).deleteRecursively()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        initKoin()
        if (initDatabase) {
            println("creating database")
            PodcastsDatabase.Schema.create(get<SqlDriver>())
        }
        declare<TimeZone> { timeZone }
        declare<Clock> { TestClock() }
        declare<Path> { Paths.get(TEMP_DIR).resolve("${UUID.randomUUID()}.preferences_pb").toOkioPath() }
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    protected fun podcast(
        id: Long,
        subscribed: Boolean,
    ) = Podcast(
        id = id,
        guid = "",
        title = "",
        description = "",
        author = "",
        owner = "",
        url = "",
        link = "",
        image = "",
        artwork = "",
        explicit = false,
        episodeCount = 0,
        categories = listOf(),
        subscribed = subscribed,
        autoDownloadEpisodes = false,
        newEpisodeNotifications = false,
        subscribedDate = null,
        hasNewEpisodes = false,
        autoAddToQueue = false,
        showCompletedEpisodes = false,
        episodeSortOrder = EpisodeSortOrder.DATE_PUBLISHED_DESC,
    )

    protected fun episode(
        id: String,
        podcastId: Long,
        downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
        queuePosition: Int = -1,
        isFavorite: Boolean = false,
    ) = Episode(
        id = id,
        podcastId = podcastId,
        podcastName = "",
        podcastAuthor = "",
        podcastImageUrl = "",
        podcastLink = "",
        podcastUrl = "",
        title = "",
        description = "",
        link = "",
        enclosureUrl = "",
        datePublished = 0,
        duration = null,
        explicit = false,
        episode = null,
        season = null,
        progressInSeconds = 0,
        downloadStatus = downloadStatus,
        downloadProgress = 0.0,
        downloadBlocked = false,
        downloadedAt = null,
        queuePosition = queuePosition,
        completedAt = null,
        isFavorite = isFavorite,
        needsDownload = false,
    )

    private fun initKoin() {
        initKoin {
            module {
                single<AppInfo> {
                    object : AppInfo {
                        override val isDebug = true
                    }
                }

                single<EpisodeDownloader> {
                    object : EpisodeDownloader {
                        override fun add(episode: Episode) {
                            println("Add episode ${episode.id} for download")
                        }

                        override fun remove(episode: Episode) {
                            println("Remove episode ${episode.id} download")
                        }

                        override fun cancel(episode: Episode) {
                            println("Cancel episode ${episode.id} download")
                        }

                        override fun setAllowOnWifiOnly(onWifiOnly: Boolean) {
                            println("Set allow on wifi only: $onWifiOnly")
                        }
                    }
                }

                single<Logger> {
                    object : Logger {
                        override fun d(
                            tag: String,
                            message: String,
                        ) {
                            println("$tag: $message")
                        }

                        override fun v(
                            tag: String,
                            message: String,
                        ) {
                            println("$tag: $message")
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TEMP_DIR = "temp"
    }
}
