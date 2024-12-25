package com.ramitsuri.podcasts.database.dao

import com.ramitsuri.podcasts.BackupDataQueries
import com.ramitsuri.podcasts.database.dao.interfaces.BackupRestoreDao
import com.ramitsuri.podcasts.model.BackupData
import com.ramitsuri.podcasts.model.fromEntity
import com.ramitsuri.podcasts.model.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class BackupRestoreDaoImpl(
    private val ioDispatcher: CoroutineDispatcher,
    private val backupDataQueries: BackupDataQueries,
) : BackupRestoreDao {

    override suspend fun getData(): BackupData {
        return withContext(ioDispatcher) {
            val podcasts = async {
                backupDataQueries
                    .getPodcastEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }
            val podcastAdditionalInfo = async {
                backupDataQueries
                    .getPodcastAdditionalInfoEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }
            val episodes = async {
                backupDataQueries
                    .getEpisodeEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }
            val episodeAdditionalInfo = async {
                backupDataQueries
                    .getEpisodeAdditionalEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }
            val sessionActions = async {
                backupDataQueries
                    .getSessionActionEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }
            val categories = async {
                backupDataQueries
                    .getCategoryEntities()
                    .executeAsList()
                    .map { it.fromEntity() }
            }

            BackupData(
                podcasts = podcasts.await(),
                podcastAdditionalInfo = podcastAdditionalInfo.await(),
                episodes = episodes.await(),
                episodeAdditionalInfo = episodeAdditionalInfo.await(),
                sessionActions = sessionActions.await(),
                categories = categories.await(),
                prefs = listOf(),
            )
        }
    }

    override suspend fun removeData() {
        withContext(ioDispatcher) {
            listOf(
                backupDataQueries::removePodcastEntities,
                backupDataQueries::removePodcastAdditionalInfoEntities,
                backupDataQueries::removeEpisodeEntities,
                backupDataQueries::removeEpisodeAdditionalEntities,
                backupDataQueries::removeCategoryEntities,
                backupDataQueries::removeSessionActionEntities,
            )
                .map {
                    async { it.invoke() }
                }
                .awaitAll()
        }
    }

    override suspend fun addData(data: BackupData) {
        withContext(ioDispatcher) {
            backupDataQueries.transaction {
                data.podcasts.forEach {
                    backupDataQueries.insertPodcastEntity(it.toEntity())
                }
                data.podcastAdditionalInfo.forEach {
                    backupDataQueries.insertPodcastAdditionalInfoEntity(it.toEntity())
                }
                data.episodes.forEach {
                    backupDataQueries.insertEpisodeEntity(it.toEntity())
                }
                data.episodeAdditionalInfo.forEach {
                    backupDataQueries.insertEpisodeAdditionalInfoEntity(it.toEntity())
                }
                data.categories.forEach {
                    backupDataQueries.insertCategoryEntity(it.toEntity())
                }
                data.sessionActions.forEach { session ->
                    backupDataQueries.insertSessionActionEntity(
                        sessionId = session.sessionId,
                        podcastId = session.podcastId,
                        episodeId = session.episodeId,
                        time = session.time,
                        action = session.action,
                        playbackSpeed = session.playbackSpeed,
                    )
                }
            }
        }
    }
}
