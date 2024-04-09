package com.ramitsuri.podcasts.android.utils

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class EpisodeFetchWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val episodeFetcher by inject<EpisodeFetcher>()

    override suspend fun doWork(): Result {
        episodeFetcher.fetchPodcastsIfNecessary(forced = true)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification =
            NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(
                    R.string.episode_fetch_worker_id,
                ),
            ).apply {
                setSmallIcon(R.drawable.media3_notification_small_icon)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setContentTitle(applicationContext.getString(R.string.episode_fetch_worker))
            }.build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object : KoinComponent {
        private const val WORK_NAME_PERIODIC = "EpisodeFetchWorker"
        private const val REPEAT_HOURS: Long = 12
        private const val NOTIFICATION_ID = NotificationId.EPISODE_FETCH_WORKER

        fun enqueuePeriodic(context: Context) {
            val builder =
                PeriodicWorkRequest
                    .Builder(EpisodeFetchWorker::class.java, REPEAT_HOURS, TimeUnit.HOURS)
                    .addTag(WORK_NAME_PERIODIC)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(false)
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .build(),
                    )

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    builder.build(),
                )
        }
    }
}
