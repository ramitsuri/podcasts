package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.DbEpisode
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

    internal constructor(dbEpisode: DbEpisode) : this(
        id = dbEpisode.id,
        podcastId = dbEpisode.podcastId,
        podcastName = dbEpisode.podcastTitle,
        podcastAuthor = dbEpisode.podcastAuthor,
        podcastImageUrl = dbEpisode.podcastImageUrl,
        podcastLink = dbEpisode.podcastLink,
        podcastUrl = dbEpisode.podcastUrl,
        title = dbEpisode.title,
        description = dbEpisode.description,
        link = dbEpisode.link,
        enclosureUrl = dbEpisode.enclosureUrl,
        datePublished = dbEpisode.datePublished,
        duration = dbEpisode.duration,
        explicit = dbEpisode.explicit,
        episode = dbEpisode.episode,
        season = dbEpisode.season,
        progressInSeconds = dbEpisode.playProgress,
        downloadStatus = dbEpisode.downloadStatus,
        downloadProgress = dbEpisode.downloadProgress,
        downloadBlocked = dbEpisode.downloadBlocked,
        downloadedAt = dbEpisode.downloadedAt,
        queuePosition = dbEpisode.queuePosition,
        completedAt = dbEpisode.completedAt,
        isFavorite = dbEpisode.isFavorite,
    )

    companion object {
        const val NOT_IN_QUEUE = -1
        const val PLAY_PROGRESS_MIN = 0
        const val PLAY_PROGRESS_MAX = Int.MAX_VALUE
    }
}
