package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.TrendingPodcastEntity
import com.ramitsuri.podcasts.model.TrendingPodcast
import kotlinx.coroutines.flow.Flow

internal interface TrendingPodcastsDao {
    fun getAll(): Flow<List<TrendingPodcastEntity>>

    suspend fun insert(trendingPodcasts: List<TrendingPodcast>)

    suspend fun deleteAll()
}
