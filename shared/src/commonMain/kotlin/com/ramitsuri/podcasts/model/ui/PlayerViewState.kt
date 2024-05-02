package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class PlayerViewState(
    val episode: Episode? = null,
    val playingState: PlayingState = PlayingState.NOT_PLAYING,
    val sleepTimer: SleepTimer = SleepTimer.None,
    val sleepTimerDuration: Duration? = null,
    val playbackSpeed: Float = 1f,
    val isCasting: Boolean = false,
    val trimSilence: Boolean = false,
    val tempPlayProgress: Float? = null,
) {
    val episodeId
        get() = episode?.id

    val podcastId
        get() = episode?.podcastId

    val episodeTitle
        get() = episode?.title ?: ""

    val podcastName
        get() = episode?.podcastName ?: ""

    val episodeArtworkUrl
        get() = episode?.podcastImageUrl ?: ""

    val isFavorite
        get() = episode?.isFavorite ?: false

    val playedDuration
        get() =
            if (episode?.isCompleted == true) {
                totalDuration ?: Duration.ZERO
            } else {
                episode?.progressInSeconds?.seconds ?: Duration.ZERO
            }

    val remainingDuration
        get() =
            if (episode?.isCompleted == true) {
                Duration.ZERO
            } else {
                episode?.remainingDuration
            }

    val progress
        get() =
            tempPlayProgress
                ?: if (episode?.isCompleted == true) {
                    1f
                } else {
                    val durationForProgress = (episode?.duration?.toFloat() ?: 1f).coerceAtLeast(1f)
                    episode
                        ?.progressInSeconds
                        ?.toFloat()
                        ?.div(durationForProgress)
                        ?.coerceIn(0f, 1f)
                        ?: 0f
                }

    val hasEverBeenPlayed
        get() = episode?.id != null

    val totalDuration
        get() = episode?.duration?.seconds
}
