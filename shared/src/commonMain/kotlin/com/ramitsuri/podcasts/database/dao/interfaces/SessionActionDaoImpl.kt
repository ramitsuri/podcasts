package com.ramitsuri.podcasts.database.dao.interfaces

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ramitsuri.podcasts.SessionActionEntity
import com.ramitsuri.podcasts.SessionHistoryQueries
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SessionActionDaoImpl(
    private val sessionHistoryQueries: SessionHistoryQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : SessionActionDao {
    override suspend fun insert(action: SessionAction) {
        withContext(ioDispatcher) {
            sessionHistoryQueries.insertOrIgnore(
                sessionId = action.sessionId,
                podcastId = action.podcastId,
                episodeId = action.episodeId,
                time = action.time,
                action = action.action,
                playbackSpeed = action.playbackSpeed,
            )
        }
    }

    override fun getSessionActionEntities(): Flow<List<SessionActionEntity>> {
        // Gets unique groups of (sessionId, podcastId, episodeId) and then gets the first row
        // from each of those groups. Idea being, get the first entry of an episode for a session.
        return sessionHistoryQueries
            .getGroupSelectors()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { groupSelectors ->
                groupSelectors.mapNotNull { groupSelector ->
                    sessionHistoryQueries.get(
                        sessionId = groupSelector.sessionId,
                        podcastId = groupSelector.podcastId,
                        episodeId = groupSelector.episodeId,
                    ).executeAsOneOrNull()
                }
                    .sortedByDescending { sessionActionEntity ->
                        sessionActionEntity.time
                    }
            }
    }
}
