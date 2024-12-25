package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.CategoryEntity
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntity
import com.ramitsuri.podcasts.EpisodeEntity
import com.ramitsuri.podcasts.PodcastAdditionalInfoEntity
import com.ramitsuri.podcasts.PodcastEntity
import com.ramitsuri.podcasts.SessionActionEntity
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    @SerialName("version")
    val version: Int = VERSION_V1,
    @SerialName("podcasts")
    val podcasts: List<PodcastData>,
    @SerialName("podcastAdditionalInfo")
    val podcastAdditionalInfo: List<PodcastAdditionalInfoData>,
    @SerialName("episodes")
    val episodes: List<EpisodeData>,
    @SerialName("episodeAdditionalInfo")
    val episodeAdditionalInfo: List<EpisodeAdditionalInfoData>,
    @SerialName("sessionActions")
    val sessionActions: List<SessionActionData>,
    @SerialName("categories")
    val categories: List<CategoryData>,
    @SerialName("prefs")
    val prefs: List<PreferenceData>,
) {
    @Serializable
    data class PodcastData(
        @SerialName("id")
        val id: Long,
        @SerialName("guid")
        val guid: String,
        @SerialName("title")
        val title: String,
        @SerialName("description")
        val description: String,
        @SerialName("author")
        val author: String,
        @SerialName("owner")
        val owner: String,
        @SerialName("url")
        val url: String,
        @SerialName("link")
        val link: String,
        @SerialName("image")
        val image: String,
        @SerialName("artwork")
        val artwork: String,
        @SerialName("explicit")
        val explicit: Boolean,
        @SerialName("episodeCount")
        val episodeCount: Int,
        @SerialName("categories")
        val categories: List<Int>,
    )

    @Serializable
    data class PodcastAdditionalInfoData(
        @SerialName("id")
        val id: Long,
        @SerialName("subscribed")
        val subscribed: Boolean,
        @SerialName("autoDownloadEpisodes")
        val autoDownloadEpisodes: Boolean,
        @SerialName("newEpisodeNotification")
        val newEpisodeNotification: Boolean,
        @SerialName("subscribedDate")
        val subscribedDate: Instant?,
        @SerialName("hasNewEpisodes")
        val hasNewEpisodes: Boolean,
        @SerialName("autoAddToQueue")
        val autoAddToQueue: Boolean,
        @SerialName("showCompletedEpisodes")
        val showCompletedEpisodes: Boolean,
        @SerialName("episodeSortOrder")
        val episodeSortOrder: EpisodeSortOrder,
    )

    @Serializable
    data class EpisodeData(
        @SerialName("id")
        val id: String,
        @SerialName("podcastId")
        val podcastId: Long,
        @SerialName("title")
        val title: String,
        @SerialName("description")
        val description: String,
        @SerialName("link")
        val link: String,
        @SerialName("enclosureUrl")
        val enclosureUrl: String,
        @SerialName("datePublished")
        val datePublished: Long,
        @SerialName("duration")
        val duration: Int?,
        @SerialName("explicit")
        val explicit: Boolean,
        @SerialName("episode")
        val episode: Int?,
        @SerialName("season")
        val season: Int?,
    )

    @Serializable
    data class EpisodeAdditionalInfoData(
        @SerialName("id")
        val id: String,
        @SerialName("playProgress")
        val playProgress: Int,
        @SerialName("downloadStatus")
        val downloadStatus: DownloadStatus,
        @SerialName("downloadProgress")
        val downloadProgress: Double,
        @SerialName("downloadBlocked")
        val downloadBlocked: Boolean,
        @SerialName("downloadedAt")
        val downloadedAt: Instant?,
        @SerialName("queuePosition")
        val queuePosition: Int,
        @SerialName("completedAt")
        val completedAt: Instant?,
        @SerialName("isFavorite")
        val isFavorite: Boolean,
        @SerialName("needsDownload")
        val needsDownload: Boolean,
    )

    @Serializable
    data class SessionActionData(
        @SerialName("id")
        val id: Long,
        @SerialName("sessionId")
        val sessionId: String,
        @SerialName("podcastId")
        val podcastId: Long,
        @SerialName("episodeId")
        val episodeId: String,
        @SerialName("time")
        val time: Instant,
        @SerialName("action")
        val action: Action,
        @SerialName("playbackSpeed")
        val playbackSpeed: Float,
    )

    @Serializable
    data class CategoryData(
        @SerialName("id")
        val id: Int,
        @SerialName("name")
        val name: String,
    )

    @Serializable
    data class PreferenceData(
        @SerialName("key")
        val key: String,
        @SerialName("value")
        val value: String,
        @SerialName("type")
        val type: String,
    )

    companion object {
        const val STRING = "string"
        const val FLOAT = "float"
        const val BOOL = "bool"
        const val INT = "int"
        const val VERSION_V1 = 1
    }
}

fun BackupData.PodcastData.toEntity() = PodcastEntity(
    id = this.id,
    guid = this.guid,
    title = this.title,
    description = this.description,
    author = this.author,
    owner = this.owner,
    url = this.url,
    link = this.link,
    image = this.image,
    artwork = this.artwork,
    explicit = this.explicit,
    episodeCount = this.episodeCount,
    categories = this.categories,
)

fun PodcastEntity.fromEntity() = BackupData.PodcastData(
    id = this.id,
    guid = this.guid,
    title = this.title,
    description = this.description,
    author = this.author,
    owner = this.owner,
    url = this.url,
    link = this.link,
    image = this.image,
    artwork = this.artwork,
    explicit = this.explicit,
    episodeCount = this.episodeCount,
    categories = this.categories,
)

fun BackupData.PodcastAdditionalInfoData.toEntity() = PodcastAdditionalInfoEntity(
    id = this.id,
    subscribed = this.subscribed,
    autoDownloadEpisodes = this.autoDownloadEpisodes,
    newEpisodeNotification = this.newEpisodeNotification,
    subscribedDate = this.subscribedDate,
    hasNewEpisodes = this.hasNewEpisodes,
    autoAddToQueue = this.autoAddToQueue,
    showCompletedEpisodes = this.showCompletedEpisodes,
    episodeSortOrder = this.episodeSortOrder,
)

fun PodcastAdditionalInfoEntity.fromEntity() = BackupData.PodcastAdditionalInfoData(
    id = this.id,
    subscribed = this.subscribed,
    autoDownloadEpisodes = this.autoDownloadEpisodes,
    newEpisodeNotification = this.newEpisodeNotification,
    subscribedDate = this.subscribedDate,
    hasNewEpisodes = this.hasNewEpisodes,
    autoAddToQueue = this.autoAddToQueue,
    showCompletedEpisodes = this.showCompletedEpisodes,
    episodeSortOrder = this.episodeSortOrder,
)

fun BackupData.EpisodeData.toEntity() = EpisodeEntity(
    id = this.id,
    podcastId = this.podcastId,
    title = this.title,
    description = this.description,
    link = this.link,
    enclosureUrl = this.enclosureUrl,
    datePublished = this.datePublished,
    duration = this.duration,
    explicit = this.explicit,
    episode = this.episode,
    season = this.season,
)

fun EpisodeEntity.fromEntity() = BackupData.EpisodeData(
    id = this.id,
    podcastId = this.podcastId,
    title = this.title,
    description = this.description,
    link = this.link,
    enclosureUrl = this.enclosureUrl,
    datePublished = this.datePublished,
    duration = this.duration,
    explicit = this.explicit,
    episode = this.episode,
    season = this.season,
)

fun BackupData.EpisodeAdditionalInfoData.toEntity() = EpisodeAdditionalInfoEntity(
    id = this.id,
    playProgress = this.playProgress,
    downloadStatus = this.downloadStatus,
    downloadProgress = this.downloadProgress,
    downloadBlocked = this.downloadBlocked,
    downloadedAt = this.downloadedAt,
    queuePosition = this.queuePosition,
    completedAt = this.completedAt,
    isFavorite = this.isFavorite,
    needsDownload = this.needsDownload,
)

fun EpisodeAdditionalInfoEntity.fromEntity() = BackupData.EpisodeAdditionalInfoData(
    id = this.id,
    playProgress = this.playProgress,
    downloadStatus = this.downloadStatus,
    downloadProgress = this.downloadProgress,
    downloadBlocked = this.downloadBlocked,
    downloadedAt = this.downloadedAt,
    queuePosition = this.queuePosition,
    completedAt = this.completedAt,
    isFavorite = this.isFavorite,
    needsDownload = this.needsDownload,
)

fun SessionActionEntity.fromEntity() = BackupData.SessionActionData(
    id = this.id,
    sessionId = this.sessionId,
    podcastId = this.podcastId,
    episodeId = this.episodeId,
    time = this.time,
    action = this.action,
    playbackSpeed = this.playbackSpeed,
)

fun BackupData.CategoryData.toEntity() = CategoryEntity(
    id = id,
    name = name,
)

fun CategoryEntity.fromEntity() = BackupData.CategoryData(
    id = id,
    name = name,
)

fun Map<String, Any>.fromMap() = mapNotNull { (key, value) ->
    val type = when (value) {
        is String -> BackupData.STRING
        is Float -> BackupData.FLOAT
        is Boolean -> BackupData.BOOL
        is Int -> BackupData.INT
        else -> null
    }
    if (type != null) {
        BackupData.PreferenceData(
            key = key,
            value = value.toString(),
            type = type,
        )
    } else {
        null
    }
}
