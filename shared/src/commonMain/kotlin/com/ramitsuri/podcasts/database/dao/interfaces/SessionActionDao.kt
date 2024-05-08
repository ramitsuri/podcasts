package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.SessionActionEntity
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.flow.Flow

internal interface SessionActionDao {
    suspend fun insert(action: SessionAction)

    fun getSessionActionEntities(): Flow<List<SessionActionEntity>>

    suspend fun getEpisodes(episodeIds: List<String>, podcastIds: List<Long>): List<EpisodeAndPodcastId>
}
