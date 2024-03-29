package com.ramitsuri.podcasts.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.CategoryEntity
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntity
import com.ramitsuri.podcasts.EpisodeEntity
import com.ramitsuri.podcasts.PodcastAdditionalInfoEntity
import com.ramitsuri.podcasts.PodcastEntity
import com.ramitsuri.podcasts.PodcastsDatabase
import com.ramitsuri.podcasts.SessionActionEntity
import com.ramitsuri.podcasts.model.Action
import com.ramitsuri.podcasts.model.DownloadStatus
import kotlinx.datetime.Instant

internal fun provideDatabase(driver: SqlDriver): PodcastsDatabase {
    val intToLongAdapter = IntToLongAdapter()
    val instantToLongAdapter = InstantToLongAdapter()
    val doubleToDoubleAdapter = DoubleToDoubleAdapter()
    val floatToDoubleAdapter = FloatToDoubleAdapter()
    val downloadStatusToStringAdapter = DownloadStatusToStringAdapter()
    val intListToStringAdapter = IntListToStringAdapter()
    val actionToStringAdapter = ActionToStringAdapter()

    return PodcastsDatabase(
        driver = driver,
        CategoryEntityAdapter =
            CategoryEntity.Adapter(
                idAdapter = intToLongAdapter,
            ),
        EpisodeAdditionalInfoEntityAdapter =
            EpisodeAdditionalInfoEntity.Adapter(
                playProgressAdapter = intToLongAdapter,
                downloadStatusAdapter = downloadStatusToStringAdapter,
                queuePositionAdapter = intToLongAdapter,
                completedAtAdapter = instantToLongAdapter,
                downloadProgressAdapter = doubleToDoubleAdapter,
                downloadedAtAdapter = instantToLongAdapter,
            ),
        EpisodeEntityAdapter =
            EpisodeEntity.Adapter(
                episodeAdapter = intToLongAdapter,
                seasonAdapter = intToLongAdapter,
                durationAdapter = intToLongAdapter,
            ),
        PodcastAdditionalInfoEntityAdapter =
            PodcastAdditionalInfoEntity.Adapter(
                subscribedDateAdapter = instantToLongAdapter,
            ),
        PodcastEntityAdapter =
            PodcastEntity.Adapter(
                episodeCountAdapter = intToLongAdapter,
                categoriesAdapter = intListToStringAdapter,
            ),
        SessionActionEntityAdapter =
            SessionActionEntity.Adapter(
                timeAdapter = instantToLongAdapter,
                actionAdapter = actionToStringAdapter,
                playbackSpeedAdapter = floatToDoubleAdapter,
            ),
    )
}

private class InstantToLongAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant {
        return Instant.fromEpochMilliseconds(databaseValue)
    }

    override fun encode(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}

private class IntToLongAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int {
        return databaseValue.toInt()
    }

    override fun encode(value: Int): Long {
        return value.toLong()
    }
}

private class DoubleToDoubleAdapter : ColumnAdapter<Double, Double> {
    override fun decode(databaseValue: Double): Double {
        return databaseValue
    }

    override fun encode(value: Double): Double {
        return value
    }
}

private class FloatToDoubleAdapter : ColumnAdapter<Float, Double> {
    override fun decode(databaseValue: Double): Float {
        return databaseValue.toFloat()
    }

    override fun encode(value: Float): Double {
        return value.toDouble()
    }
}

private class DownloadStatusToStringAdapter : ColumnAdapter<DownloadStatus, String> {
    override fun decode(databaseValue: String): DownloadStatus {
        return DownloadStatus.fromValue(databaseValue)
    }

    override fun encode(value: DownloadStatus): String {
        return value.value
    }
}

private class IntListToStringAdapter : ColumnAdapter<List<Int>, String> {
    override fun decode(databaseValue: String): List<Int> {
        if (databaseValue.isEmpty()) {
            return emptyList()
        }
        return databaseValue.split(SEPARATOR).map { it.toInt() }
    }

    override fun encode(value: List<Int>): String {
        return value.joinToString(SEPARATOR)
    }

    companion object {
        private const val SEPARATOR = ";;;"
    }
}

private class ActionToStringAdapter : ColumnAdapter<Action, String> {
    override fun decode(databaseValue: String): Action {
        return Action.fromValue(databaseValue)
    }

    override fun encode(value: Action): String {
        return value.value
    }
}
