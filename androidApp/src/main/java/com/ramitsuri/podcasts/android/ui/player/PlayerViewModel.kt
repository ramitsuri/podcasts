package com.ramitsuri.podcasts.android.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ramitsuri.podcasts.model.ui.PlayerViewState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlayerViewModel(
    private val playerController: PlayerController,
    private val longLivingScope: CoroutineScope,
    private val settings: Settings,
    private val episodesRepository: EpisodesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerViewState())
    val state = _state.asStateFlow()

    private var updateEpisodeStateJob: Job? = null

    init {
        viewModelScope.launch {
            episodesRepository.getCurrentEpisode().collect { currentEpisode ->
                if (currentEpisode != null) {
                    updateEpisodeState(currentEpisode.id)
                }
            }
        }

        viewModelScope.launch {
            settings.getPlayingStateFlow().collect { playingState ->
                _state.update { it.copy(playingState = playingState) }
            }
        }
    }

    private fun updateEpisodeState(episodeId: String) {
        updateEpisodeStateJob?.cancel()
        updateEpisodeStateJob =
            viewModelScope.launch {
                combine(
                    episodesRepository.getEpisodeFlow(episodeId),
                    settings.getPlaybackSpeedFlow(),
                ) { episode, speed ->
                    Pair(episode, speed)
                }.collect { (episode, speed) ->
                    if (episode != null) {
                        _state.update {
                            val duration = episode.duration
                            val durationForProgress = (duration?.toFloat() ?: 1f).coerceAtLeast(1f)
                            val progressPercent =
                                episode
                                    .progressInSeconds
                                    .toFloat()
                                    .div(durationForProgress)
                                    .coerceIn(0f, 1f)
                            val remainingDuration = duration?.minus(episode.progressInSeconds)
                            it.copy(
                                hasEverBeenPlayed = true,
                                episodeTitle = episode.title,
                                episodeArtworkUrl = episode.podcastImageUrl,
                                podcastName = episode.podcastName,
                                sleepTimer = SleepTimer.None,
                                playbackSpeed = speed,
                                isCasting = false,
                                progress = progressPercent,
                                playedDuration = episode.progressInSeconds.seconds,
                                remainingDuration = remainingDuration?.seconds,
                                totalDuration = duration?.seconds,
                            )
                        }
                    }
                }
            }
    }

    fun onPlayClicked() {
        longLivingScope.launch {
            val episode = episodesRepository.getCurrentEpisode().firstOrNull()
            if (episode != null) {
                playerController.play(episode)
            }
        }
    }

    fun onPauseClicked() {
        playerController.pause()
    }

    fun onSkipRequested(by: Duration = 30.seconds) {
        playerController.skip(by)
    }

    fun onReplayRequested(by: Duration = 10.seconds) {
        playerController.replay(by)
    }

    fun onSeekRequested(toPercentOfDuration: Float) {
        val duration = _state.value.totalDuration
        if (duration != null) {
            playerController.seek(duration.times(toPercentOfDuration.toDouble()))
        }
    }

    fun onSpeedChangeRequested(speed: Float) {
        longLivingScope.launch {
            val rounded = speed
                .times(1000)
                .roundToInt()
                .div(1000f)
                .coerceIn(0.1f, 3.0f)
            playerController.setPlaybackSpeed(rounded)
            settings.setPlaybackSpeed(rounded)
            _state.update { it.copy(playbackSpeed = rounded) }
        }
    }

    fun onSpeedIncreaseRequested() {
        val currentSpeed = _state.value.playbackSpeed
        val newSpeed = currentSpeed.plus(0.1f)
        onSpeedChangeRequested(newSpeed)
    }

    fun onSpeedDecreaseRequested() {
        val currentSpeed = _state.value.playbackSpeed
        val newSpeed = currentSpeed.minus(0.1f)
        onSpeedChangeRequested(newSpeed)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory, KoinComponent {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(
                        playerController = get<PlayerController>(),
                        longLivingScope = get<CoroutineScope>(),
                        settings = get<Settings>(),
                        episodesRepository = get<EpisodesRepository>(),
                    ) as T
                }
            }
    }
}
