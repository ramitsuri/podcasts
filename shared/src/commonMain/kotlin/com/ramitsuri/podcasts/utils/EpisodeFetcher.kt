package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
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
                fetchPodcastsIfNecessary()
            }
        }
    }

    suspend fun fetchPodcastsIfNecessary(forced: Boolean = false) {
        refreshPodcastsMutex.withLock {
            val lastFetchTime = settings.getLastEpisodeFetchTime()
            val now = clock.now()
            if (forced || now.minus(lastFetchTime) > FETCH_THRESHOLD_HOURS.hours) {
                LogHelper.d(TAG, "Fetch threshold met, fetching now")
                val result = repository.refreshPodcasts()
                if (result is PodcastResult.Failure) {
                    LogHelper.v(TAG, "Failed to fetch podcasts: ${result.error}")
                    return
                }
                settings.setLastEpisodeFetchTime()
            } else {
                LogHelper.d(TAG, "Fetch threshold not met and not forced, skipping")
            }
        }
    }

    companion object {
        private const val TAG = "EpisodeFetcher"
        private const val FETCH_THRESHOLD_HOURS = 6
    }
}
