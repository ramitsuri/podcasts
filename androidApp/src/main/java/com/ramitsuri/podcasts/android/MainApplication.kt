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
import com.ramitsuri.podcasts.AppInfo
import com.ramitsuri.podcasts.android.media.DownloadManagerListener
import com.ramitsuri.podcasts.android.media.PlayerControllerImpl
import com.ramitsuri.podcasts.android.utils.Constants
import com.ramitsuri.podcasts.initKoin
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import java.io.File

@UnstableApi
class MainApplication : Application(), KoinComponent {
    private val playerController by inject<PlayerController>()

    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
        playerController.initializePlayer()
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

                single<File> {
                    val appExternalDir = applicationContext.getExternalFilesDir(null) ?: applicationContext.filesDir
                    File(appExternalDir, Constants.DOWNLOADS_DIRECTORY)
                }

                single<Cache> {
                    SimpleCache(
                        get<File>(),
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
                        maxParallelDownloads = 1
                    }
                }

                single<PlayerController> {
                    PlayerControllerImpl(
                        longLivingScope = get<CoroutineScope>(),
                        context = get<Application>(),
                        settings = get<Settings>(),
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
