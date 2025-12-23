package com.ramitsuri.podcasts.database.dao.interfaces

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.ramitsuri.podcasts.SessionActionEntity
import com.ramitsuri.podcasts.SessionHistoryQueries
import com.ramitsuri.podcasts.model.EpisodeAndPodcastId
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

    override suspend fun getEpisodes(
        episodeIds: List<String>,
        podcastIds: List<Long>,
    ): List<EpisodeAndPodcastId> {
        return withContext(ioDispatcher) {
            sessionHistoryQueries
                .getEpisodes(
                    episodeIds = episodeIds.take(SQLITE_MAX_VARIABLE_NUMBER),
                    podcastIds = podcastIds.take(SQLITE_MAX_VARIABLE_NUMBER),
                )
                .executeAsList()
                .map {
                    EpisodeAndPodcastId(
                        episodeId = it.episodeId,
                        podcastId = it.podcastId,
                    )
                }
        }
    }

    override suspend fun getAll(): List<SessionActionEntity> {
        return withContext(ioDispatcher) {
            sessionHistoryQueries
                .getAll()
                .executeAsList()
        }
    }

    override fun hasSessions(): Flow<Boolean> {
        return sessionHistoryQueries
            .getCount()
            .asFlow()
            .mapToOne(ioDispatcher)
            .map { it > 0 }
    }

    companion object {
        // Limit max vars otherwise causes exception - too many SQL variables (Sqlite code 1 SQLITE_ERROR)
        private const val SQLITE_MAX_VARIABLE_NUMBER = 999
    }
}
