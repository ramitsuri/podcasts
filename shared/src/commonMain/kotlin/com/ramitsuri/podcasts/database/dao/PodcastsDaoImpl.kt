package com.ramitsuri.podcasts.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ramitsuri.podcasts.GetAllPodcasts
import com.ramitsuri.podcasts.GetAllSubscribedPodcasts
import com.ramitsuri.podcasts.GetPodcast
import com.ramitsuri.podcasts.PodcastAdditionalInfoEntity
import com.ramitsuri.podcasts.PodcastAdditionalInfoEntityQueries
import com.ramitsuri.podcasts.PodcastEntity
import com.ramitsuri.podcasts.PodcastEntityQueries
import com.ramitsuri.podcasts.database.dao.interfaces.PodcastsDao
import com.ramitsuri.podcasts.model.Podcast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class PodcastsDaoImpl(
    private val podcastEntityQueries: PodcastEntityQueries,
    private val podcastAdditionalInfoEntityQueries: PodcastAdditionalInfoEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : PodcastsDao {
    override fun getAll(): Flow<List<GetAllPodcasts>> {
        return podcastEntityQueries
            .getAllPodcasts()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override suspend fun getAllSubscribed(): List<GetAllSubscribedPodcasts> {
        return withContext(ioDispatcher) {
            podcastEntityQueries
                .getAllSubscribedPodcasts()
                .executeAsList()
        }
    }

    override fun getAllSubscribedFlow(): Flow<List<GetAllSubscribedPodcasts>> {
        return podcastEntityQueries
            .getAllSubscribedPodcasts()
            .asFlow()
            .mapToList(ioDispatcher)
    }

    override fun getFlow(id: Long): Flow<GetPodcast?> {
        return podcastEntityQueries
            .getPodcast(id)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
    }

    override suspend fun get(id: Long): GetPodcast? {
        return withContext(ioDispatcher) {
            podcastEntityQueries
                .getPodcast(id)
                .executeAsOneOrNull()
        }
    }

    override suspend fun insert(podcasts: List<Podcast>) {
        withContext(ioDispatcher) {
            podcasts.forEach {
                insert(it)
            }
        }
    }

    override suspend fun updateSubscribed(
        id: Long,
        subscribed: Boolean,
        actionDate: Instant,
    ) {
        withContext(ioDispatcher) {
            podcastAdditionalInfoEntityQueries.transaction {
                podcastAdditionalInfoEntityQueries.updateSubscribed(id = id, subscribed = subscribed)
                if (subscribed) {
                    podcastAdditionalInfoEntityQueries.updateSubscribedDate(id = id, subscribedDate = actionDate)
                }
            }
        }
    }

    override suspend fun updateAutoDownloadEpisodes(
        id: Long,
        autoDownloadEpisodes: Boolean,
    ) {
        withContext(ioDispatcher) {
            podcastAdditionalInfoEntityQueries.updateAutoDownloadEpisodes(
                id = id,
                autoDownloadEpisodes = autoDownloadEpisodes,
            )
        }
    }

    override suspend fun updateNewEpisodeNotification(
        id: Long,
        showNewEpisodeNotification: Boolean,
    ) {
        withContext(ioDispatcher) {
            podcastAdditionalInfoEntityQueries.updateNewEpisodeNotification(
                id = id,
                newEpisodeNotification = showNewEpisodeNotification,
            )
        }
    }

    private fun insert(podcast: Podcast) {
        podcastEntityQueries.insertOrReplace(
            PodcastEntity(
                id = podcast.id,
                guid = podcast.guid,
                title = podcast.title,
                description = podcast.description,
                author = podcast.author,
                owner = podcast.owner,
                url = podcast.url,
                link = podcast.link,
                image = podcast.image,
                artwork = podcast.artwork,
                explicit = podcast.explicit,
                episodeCount = podcast.episodeCount,
                categories = podcast.categories.map { it.id },
            ),
        )
        podcastAdditionalInfoEntityQueries.insertOrIgnore(
            PodcastAdditionalInfoEntity(
                id = podcast.id,
                subscribed = podcast.subscribed,
                autoDownloadEpisodes = podcast.autoDownloadEpisodes,
                newEpisodeNotification = podcast.newEpisodeNotifications,
                subscribedDate = podcast.subscribedDate,
            ),
        )
    }
}
