package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

class EpisodeFetcher(
    private val repository: PodcastsAndEpisodesRepository,
    private val settings: Settings,
    private val clock: Clock,
    private val foregroundStateObserver: ForegroundStateObserver,
    private val longLivingScope: CoroutineScope,
    private val isDebug: Boolean,
) {
    private val refreshPodcastsMutex = Mutex()

    fun startForegroundStateBasedFetcher() {
        if (isDebug) {
            LogHelper.d(TAG, "Skipping in debug build")
            return
        }
        longLivingScope.launch {
            foregroundStateObserver.state.collect { foregroundState ->
                if (!foregroundState.isInForeground) {
                    LogHelper.d(TAG, "App in background")
                    return@collect
                }
                LogHelper.d(TAG, "App in foreground, will fetch episodes if necessary")
                fetchPodcastsIfNecessary(forced = false, episodeDownloadAllowed = true)
            }
        }
    }

    suspend fun fetchPodcastsIfNecessary(
        forced: Boolean,
        episodeDownloadAllowed: Boolean,
    ) {
        refreshPodcastsMutex.withLock {
            val lastFetchTime = settings.getLastEpisodeFetchTime().first()
            val now = clock.now()
            val fetchFromNetwork = forced || now.minus(lastFetchTime) > FETCH_THRESHOLD_HOURS.hours
            val result =
                repository.refreshPodcasts(
                    fetchFromNetwork = fetchFromNetwork,
                    episodeDownloadAllowed = episodeDownloadAllowed,
                )
            if (result is PodcastResult.Failure) {
                LogHelper.v(TAG, "Failed to fetch podcasts: ${result.error}")
            } else {
                if (fetchFromNetwork) {
                    settings.setLastEpisodeFetchTime()
                }
            }
        }
    }

    companion object {
        private const val TAG = "EpisodeFetcher"
        private const val FETCH_THRESHOLD_HOURS = 6
    }
}
