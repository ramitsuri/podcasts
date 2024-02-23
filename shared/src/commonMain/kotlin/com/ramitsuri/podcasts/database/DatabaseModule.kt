package com.ramitsuri.podcasts.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.ramitsuri.podcasts.CategoryEntity
import com.ramitsuri.podcasts.EpisodeAdditionalInfoEntity
import com.ramitsuri.podcasts.EpisodeEntity
import com.ramitsuri.podcasts.PodcastAdditionalInfoEntity
import com.ramitsuri.podcasts.PodcastEntity
import com.ramitsuri.podcasts.PodcastsDatabase
import com.ramitsuri.podcasts.model.DownloadStatus
import kotlinx.datetime.Instant

internal fun provideDatabase(driver: SqlDriver): PodcastsDatabase {
    return PodcastsDatabase(
        driver = driver,
        CategoryEntityAdapter =
            CategoryEntity.Adapter(
                idAdapter = IntToLongAdapter(),
            ),
        EpisodeAdditionalInfoEntityAdapter =
            EpisodeAdditionalInfoEntity.Adapter(
                playProgressAdapter = IntToLongAdapter(),
                downloadStatusAdapter = DownloadStatusToString(),
                queuePositionAdapter = IntToLongAdapter(),
                completedAtAdapter = InstantToLongAdapter(),
                downloadProgressAdapter = DoubleToDoubleAdapter(),
            ),
        EpisodeEntityAdapter =
            EpisodeEntity.Adapter(
                episodeAdapter = IntToLongAdapter(),
                seasonAdapter = IntToLongAdapter(),
            ),
        PodcastAdditionalInfoEntityAdapter =
            PodcastAdditionalInfoEntity.Adapter(
                subscribedDateAdapter = InstantToLongAdapter(),
                lastRefreshDateAdapter = InstantToLongAdapter(),
            ),
        PodcastEntityAdapter =
            PodcastEntity.Adapter(
                episodeCountAdapter = IntToLongAdapter(),
                categoriesAdapter = IntListToString(),
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

private class DownloadStatusToString : ColumnAdapter<DownloadStatus, String> {
    override fun decode(databaseValue: String): DownloadStatus {
        return DownloadStatus.fromValue(databaseValue)
    }

    override fun encode(value: DownloadStatus): String {
        return value.value
    }
}

private class IntListToString : ColumnAdapter<List<Int>, String> {
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
