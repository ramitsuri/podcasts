package com.ramitsuri.podcasts

import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode
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
            }
        }
    }
}
