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
import com.ramitsuri.podcasts.android.BuildConfig
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.utils.EpisodeFetcher
import com.ramitsuri.podcasts.utils.LogHelper
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class EpisodeFetchWorker(
    private val episodeFetcher: EpisodeFetcher,
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogHelper.d(TAG, "Starting work")
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
        private const val TAG = "EpisodeFetchWorker"
        private const val WORK_NAME_PERIODIC = "EpisodeFetchWorker"
        private const val REPEAT_HOURS: Long = 12
        private const val NOTIFICATION_ID = NotificationId.EPISODE_FETCH_WORKER

        fun enqueuePeriodic(context: Context) {
            if (BuildConfig.DEBUG) {
                LogHelper.d(TAG, "Skipping in debug build")
                return
            }
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
