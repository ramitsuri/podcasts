package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.GetDownloadedEpisodes
import com.ramitsuri.podcasts.GetEpisode
import com.ramitsuri.podcasts.GetEpisodesForPodcast
import com.ramitsuri.podcasts.GetEpisodesForPodcasts
import com.ramitsuri.podcasts.GetEpisodesInQueue
import com.ramitsuri.podcasts.GetFavoriteEpisodes
import com.ramitsuri.podcasts.network.model.EpisodeDto
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Episode(
    val id: String,
    val podcastId: Long,
    val podcastName: String,
    val podcastAuthor: String,
    val podcastImageUrl: String,
    val podcastLink: String,
    val podcastUrl: String,
    val title: String,
    val description: String,
    val link: String,
    val enclosureUrl: String,
    val datePublished: Long,
    val duration: Int?,
    val explicit: Boolean,
    val episode: Int?,
    val season: Int?,
    val progressInSeconds: Int,
    val downloadStatus: DownloadStatus,
    val downloadProgress: Double,
    val downloadBlocked: Boolean,
    val downloadedAt: Instant?,
    val queuePosition: Int,
    val completedAt: Instant?,
    val isFavorite: Boolean,
) {
    val isCompleted = completedAt != null

    val datePublishedInstant = runCatching { Instant.fromEpochSeconds(datePublished) }.getOrNull()

    val remainingDuration: Duration?
        get() = (duration?.minus(progressInSeconds))?.seconds

    internal constructor(dto: EpisodeDto) : this(
        id = dto.id,
        podcastId = dto.podcastId,
        podcastName = "",
        podcastAuthor = "",
        podcastImageUrl = "",
        podcastLink = "",
        podcastUrl = "",
        title = dto.title,
        description = dto.description,
        link = dto.link,
        enclosureUrl = dto.enclosureUrl,
        datePublished = dto.datePublished,
        duration = dto.duration,
        explicit = dto.explicit == 1,
        episode = dto.episode,
        season = dto.season,
        progressInSeconds = PLAY_PROGRESS_MIN,
        downloadStatus = DownloadStatus.NOT_DOWNLOADED,
        downloadProgress = 0.0,
        downloadBlocked = false,
        downloadedAt = null,
        queuePosition = NOT_IN_QUEUE,
        completedAt = null,
        isFavorite = false,
    )

    internal constructor(getEpisode: GetEpisode) : this(
        id = getEpisode.id,
        podcastId = getEpisode.podcastId,
        podcastName = getEpisode.podcastTitle,
        podcastAuthor = getEpisode.podcastAuthor,
        podcastImageUrl = getEpisode.podcastImageUrl,
        podcastLink = getEpisode.podcastLink,
        podcastUrl = getEpisode.podcastUrl,
        title = getEpisode.title,
        description = getEpisode.description,
        link = getEpisode.link,
        enclosureUrl = getEpisode.enclosureUrl,
        datePublished = getEpisode.datePublished,
        duration = getEpisode.duration,
        explicit = getEpisode.explicit,
        episode = getEpisode.episode,
        season = getEpisode.season,
        progressInSeconds = getEpisode.playProgress,
        downloadStatus = getEpisode.downloadStatus,
        downloadProgress = getEpisode.downloadProgress,
        downloadBlocked = getEpisode.downloadBlocked,
        downloadedAt = getEpisode.downloadedAt,
        queuePosition = getEpisode.queuePosition,
        completedAt = getEpisode.completedAt,
        isFavorite = getEpisode.isFavorite,
    )

    internal constructor(getEpisodesForPodcast: GetEpisodesForPodcast) : this(
        id = getEpisodesForPodcast.id,
        podcastId = getEpisodesForPodcast.podcastId,
        podcastName = getEpisodesForPodcast.podcastTitle,
        podcastAuthor = getEpisodesForPodcast.podcastAuthor,
        podcastImageUrl = getEpisodesForPodcast.podcastImageUrl,
        podcastLink = getEpisodesForPodcast.podcastLink,
        podcastUrl = getEpisodesForPodcast.podcastUrl,
        title = getEpisodesForPodcast.title,
        description = getEpisodesForPodcast.description,
        link = getEpisodesForPodcast.link,
        enclosureUrl = getEpisodesForPodcast.enclosureUrl,
        datePublished = getEpisodesForPodcast.datePublished,
        duration = getEpisodesForPodcast.duration,
        explicit = getEpisodesForPodcast.explicit,
        episode = getEpisodesForPodcast.episode,
        season = getEpisodesForPodcast.season,
        progressInSeconds = getEpisodesForPodcast.playProgress,
        downloadStatus = getEpisodesForPodcast.downloadStatus,
        downloadProgress = getEpisodesForPodcast.downloadProgress,
        downloadBlocked = getEpisodesForPodcast.downloadBlocked,
        downloadedAt = getEpisodesForPodcast.downloadedAt,
        queuePosition = getEpisodesForPodcast.queuePosition,
        completedAt = getEpisodesForPodcast.completedAt,
        isFavorite = getEpisodesForPodcast.isFavorite,
    )

    internal constructor(getEpisodesForPodcasts: GetEpisodesForPodcasts) : this(
        id = getEpisodesForPodcasts.id,
        podcastId = getEpisodesForPodcasts.podcastId,
        podcastName = getEpisodesForPodcasts.podcastTitle,
        podcastAuthor = getEpisodesForPodcasts.podcastAuthor,
        podcastImageUrl = getEpisodesForPodcasts.podcastImageUrl,
        podcastLink = getEpisodesForPodcasts.podcastLink,
        podcastUrl = getEpisodesForPodcasts.podcastUrl,
        title = getEpisodesForPodcasts.title,
        description = getEpisodesForPodcasts.description,
        link = getEpisodesForPodcasts.link,
        enclosureUrl = getEpisodesForPodcasts.enclosureUrl,
        datePublished = getEpisodesForPodcasts.datePublished,
        duration = getEpisodesForPodcasts.duration,
        explicit = getEpisodesForPodcasts.explicit,
        episode = getEpisodesForPodcasts.episode,
        season = getEpisodesForPodcasts.season,
        progressInSeconds = getEpisodesForPodcasts.playProgress,
        downloadStatus = getEpisodesForPodcasts.downloadStatus,
        downloadProgress = getEpisodesForPodcasts.downloadProgress,
        downloadBlocked = getEpisodesForPodcasts.downloadBlocked,
        downloadedAt = getEpisodesForPodcasts.downloadedAt,
        queuePosition = getEpisodesForPodcasts.queuePosition,
        completedAt = getEpisodesForPodcasts.completedAt,
        isFavorite = getEpisodesForPodcasts.isFavorite,
    )

    internal constructor(getEpisodesInQueue: GetEpisodesInQueue) : this(
        id = getEpisodesInQueue.id,
        podcastId = getEpisodesInQueue.podcastId,
        podcastName = getEpisodesInQueue.podcastTitle,
        podcastAuthor = getEpisodesInQueue.podcastAuthor,
        podcastImageUrl = getEpisodesInQueue.podcastImageUrl,
        podcastLink = getEpisodesInQueue.podcastLink,
        podcastUrl = getEpisodesInQueue.podcastUrl,
        title = getEpisodesInQueue.title,
        description = getEpisodesInQueue.description,
        link = getEpisodesInQueue.link,
        enclosureUrl = getEpisodesInQueue.enclosureUrl,
        datePublished = getEpisodesInQueue.datePublished,
        duration = getEpisodesInQueue.duration,
        explicit = getEpisodesInQueue.explicit,
        episode = getEpisodesInQueue.episode,
        season = getEpisodesInQueue.season,
        progressInSeconds = getEpisodesInQueue.playProgress,
        downloadStatus = getEpisodesInQueue.downloadStatus,
        downloadProgress = getEpisodesInQueue.downloadProgress,
        downloadBlocked = getEpisodesInQueue.downloadBlocked,
        downloadedAt = getEpisodesInQueue.downloadedAt,
        queuePosition = getEpisodesInQueue.queuePosition,
        completedAt = getEpisodesInQueue.completedAt,
        isFavorite = getEpisodesInQueue.isFavorite,
    )

    internal constructor(getDownloadedEpisodes: GetDownloadedEpisodes) : this(
        id = getDownloadedEpisodes.id,
        podcastId = getDownloadedEpisodes.podcastId,
        podcastName = getDownloadedEpisodes.podcastTitle,
        podcastAuthor = getDownloadedEpisodes.podcastAuthor,
        podcastImageUrl = getDownloadedEpisodes.podcastImageUrl,
        podcastLink = getDownloadedEpisodes.podcastLink,
        podcastUrl = getDownloadedEpisodes.podcastUrl,
        title = getDownloadedEpisodes.title,
        description = getDownloadedEpisodes.description,
        link = getDownloadedEpisodes.link,
        enclosureUrl = getDownloadedEpisodes.enclosureUrl,
        datePublished = getDownloadedEpisodes.datePublished,
        duration = getDownloadedEpisodes.duration,
        explicit = getDownloadedEpisodes.explicit,
        episode = getDownloadedEpisodes.episode,
        season = getDownloadedEpisodes.season,
        progressInSeconds = getDownloadedEpisodes.playProgress,
        downloadStatus = getDownloadedEpisodes.downloadStatus,
        downloadProgress = getDownloadedEpisodes.downloadProgress,
        downloadBlocked = getDownloadedEpisodes.downloadBlocked,
        downloadedAt = getDownloadedEpisodes.downloadedAt,
        queuePosition = getDownloadedEpisodes.queuePosition,
        completedAt = getDownloadedEpisodes.completedAt,
        isFavorite = getDownloadedEpisodes.isFavorite,
    )

    internal constructor(getFavoriteEpisodes: GetFavoriteEpisodes) : this(
        id = getFavoriteEpisodes.id,
        podcastId = getFavoriteEpisodes.podcastId,
        podcastName = getFavoriteEpisodes.podcastTitle,
        podcastAuthor = getFavoriteEpisodes.podcastAuthor,
        podcastImageUrl = getFavoriteEpisodes.podcastImageUrl,
        podcastLink = getFavoriteEpisodes.podcastLink,
        podcastUrl = getFavoriteEpisodes.podcastUrl,
        title = getFavoriteEpisodes.title,
        description = getFavoriteEpisodes.description,
        link = getFavoriteEpisodes.link,
        enclosureUrl = getFavoriteEpisodes.enclosureUrl,
        datePublished = getFavoriteEpisodes.datePublished,
        duration = getFavoriteEpisodes.duration,
        explicit = getFavoriteEpisodes.explicit,
        episode = getFavoriteEpisodes.episode,
        season = getFavoriteEpisodes.season,
        progressInSeconds = getFavoriteEpisodes.playProgress,
        downloadStatus = getFavoriteEpisodes.downloadStatus,
        downloadProgress = getFavoriteEpisodes.downloadProgress,
        downloadBlocked = getFavoriteEpisodes.downloadBlocked,
        downloadedAt = getFavoriteEpisodes.downloadedAt,
        queuePosition = getFavoriteEpisodes.queuePosition,
        completedAt = getFavoriteEpisodes.completedAt,
        isFavorite = getFavoriteEpisodes.isFavorite,
    )

    companion object {
        const val NOT_IN_QUEUE = -1
        const val PLAY_PROGRESS_MIN = 0
        const val PLAY_PROGRESS_MAX = Int.MAX_VALUE
    }
}
