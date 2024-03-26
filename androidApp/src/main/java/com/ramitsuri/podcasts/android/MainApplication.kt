package com.ramitsuri.podcasts.android

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.ramitsuri.podcasts.AppInfo
import com.ramitsuri.podcasts.android.media.DownloadManagerListener
import com.ramitsuri.podcasts.android.media.EpisodeDownloaderImpl
import com.ramitsuri.podcasts.android.media.PlayerControllerImpl
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.initKoin
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

@UnstableApi
class MainApplication : Application(), ImageLoaderFactory, KoinComponent {
    private val playerController by inject<PlayerController>()

    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
        playerController.initializePlayer()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("downloaded_images"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initDependencyInjection() {
        initKoin(
            module {
                single<Application> {
                    this@MainApplication
                }

                single<DatabaseProvider> {
                    StandaloneDatabaseProvider(applicationContext)
                }

                single<Cache> {
                    val file = applicationContext.filesDir.resolve("downloaded_episodes")
                    SimpleCache(
                        file,
                        NoOpCacheEvictor(),
                        get<DatabaseProvider>(),
                    )
                }

                single<DownloadManager> {
                    DownloadManager(
                        applicationContext,
                        get<DatabaseProvider>(),
                        get<Cache>(),
                        DefaultHttpDataSource.Factory(),
                        Dispatchers.IO.limitedParallelism(4).asExecutor(),
                    ).apply {
                        maxParallelDownloads = 3
                    }
                }

                single<PlayerController> {
                    PlayerControllerImpl(
                        context = get<Application>(),
                    )
                }

                single<EpisodeDownloader> {
                    EpisodeDownloaderImpl(
                        appContext = get<Application>(),
                    )
                }

                factory<AppInfo> {
                    AndroidAppInfo()
                }

                factory<DownloadManagerListener> {
                    DownloadManagerListener(
                        episodesRepository = get<EpisodesRepository>(),
                        longLivingScope = get<CoroutineScope>(),
                    )
                }
            },
        )
    }
}
