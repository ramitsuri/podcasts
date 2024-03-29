package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.SessionHistoryQueries
import com.ramitsuri.podcasts.model.SessionAction
import kotlinx.coroutines.CoroutineDispatcher
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
}
