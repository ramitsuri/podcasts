package com.ramitsuri.podcasts

import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.utils.Logger
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class BaseTest(private val initDatabase: Boolean = true) : KoinTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
        if (Path("test.db").deleteIfExists()) {
            println("deleted database file")
        }
    }

    @BeforeTest
    fun setup() {
        initKoin()
        if (initDatabase) {
            println("creating database")
            PodcastsDatabase.Schema.create(get<SqlDriver>())
        }
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
                    }
                }

                single<Logger> {
                    object : Logger {
                        override fun toggleRemoteLogging(enable: Boolean) {
                            println("Toggle remote logging $enable")
                        }

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
}
