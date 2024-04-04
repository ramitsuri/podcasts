package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class EpisodeFetcher(
    private val repository: PodcastsAndEpisodesRepository,
    private val settings: Settings,
    private val clock: Clock,
    private val foregroundStateObserver: ForegroundStateObserver,
    private val longLivingScope: CoroutineScope,
    private val episodeDownloader: EpisodeDownloader,
) {
    private val refreshPodcastsMutex = Mutex()

    fun startForegroundStateBasedFetcher() {
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

    suspend fun fetchPodcastsIfNecessary() {
        refreshPodcastsMutex.withLock {
            val lastFetchTime = settings.getLastEpisodeFetchTime()
            val now = clock.now()
            if (now.minus(lastFetchTime) > FETCH_THRESHOLD_MINUTES.minutes) {
                LogHelper.d(TAG, "Last fetched more than 5 minutes ago, fetching now")
                val result = repository.refreshPodcasts()
                if (result is PodcastResult.Failure) {
                    LogHelper.v(TAG, "Failed to refresh podcasts: ${result.error}")
                    return
                }
                result as PodcastResult.Success
                settings.setLastEpisodeFetchTime()
                result.data.autoDownloadableEpisodes.forEach { episode ->
                    episodeDownloader.add(episode)
                }
            } else {
                LogHelper.d(TAG, "Last fetched less than 5 minutes ago, skipping")
            }
        }
    }

    companion object {
        private const val TAG = "EpisodeFetcher"
        private const val FETCH_THRESHOLD_MINUTES = 5
    }
}
