package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.TrendingPodcastsDao
import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.TrendingPodcast
import com.ramitsuri.podcasts.network.api.interfaces.PodcastsApi
import com.ramitsuri.podcasts.network.model.TrendingPodcastsRequest
import com.ramitsuri.podcasts.utils.CategoryHelper
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class TrendingPodcastsRepository internal constructor(
    private val podcastsApi: PodcastsApi,
    private val trendingPodcastsDao: TrendingPodcastsDao,
    private val categoryHelper: CategoryHelper,
    private val clock: Clock,
) {
    fun getAllFlow(): Flow<List<TrendingPodcast>> {
        return trendingPodcastsDao
            .getAll()
            .map { trendingPodcasts ->
                trendingPodcasts.map { trendingPodcastEntity ->
                    val categories = categoryHelper.get(trendingPodcastEntity.categories)
                    TrendingPodcast(trendingPodcastEntity, categories)
                }
            }
    }

    suspend fun refresh(): Boolean {
        LogHelper.d(TAG, "Refreshing trending podcasts")
        val result =
            podcastsApi.getTrending(
                request =
                    TrendingPodcastsRequest(
                        sinceEpochSeconds = clock.now().epochSeconds - REFRESH_SINCE,
                    ),
            )
        return when (result) {
            is PodcastResult.Failure -> {
                false
            }

            is PodcastResult.Success -> {
                trendingPodcastsDao.deleteAll()
                trendingPodcastsDao.insert(result.data.podcasts.map { TrendingPodcast(it) })
                true
            }
        }
    }

    companion object {
        private const val REFRESH_SINCE: Long = 30 * 24 * 60 * 60 // 30 days
        private const val TAG = "TrendingPodcastsRepository"
    }
}
