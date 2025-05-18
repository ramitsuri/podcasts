package com.ramitsuri.podcasts.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ramitsuri.podcasts.TrendingPodcastEntity
import com.ramitsuri.podcasts.TrendingPodcastEntityQueries
import com.ramitsuri.podcasts.database.dao.interfaces.TrendingPodcastsDao
import com.ramitsuri.podcasts.model.TrendingPodcast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class TrendingPodcastsDaoImpl(
    private val trendingPodcastsEntityQueries: TrendingPodcastEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : TrendingPodcastsDao {
    override fun getAll(): Flow<List<TrendingPodcastEntity>> {
        return trendingPodcastsEntityQueries
            .getAll()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun insert(trendingPodcasts: List<TrendingPodcast>) {
        withContext(ioDispatcher) {
            trendingPodcasts.forEach {
                insert(it)
            }
        }
    }

    override suspend fun deleteAll() {
        withContext(ioDispatcher) {
            trendingPodcastsEntityQueries.removeAll()
        }
    }

    private fun insert(trendingPodcast: TrendingPodcast) {
        trendingPodcastsEntityQueries.insertOrReplace(
            TrendingPodcastEntity(
                id = trendingPodcast.id,
                title = trendingPodcast.title,
                description = trendingPodcast.description,
                author = trendingPodcast.author,
                url = trendingPodcast.url,
                image = trendingPodcast.image,
                artwork = trendingPodcast.artwork,
                trendScore = trendingPodcast.trendScore,
                categories = trendingPodcast.categories.map { it.id },
            ),
        )
    }
}
