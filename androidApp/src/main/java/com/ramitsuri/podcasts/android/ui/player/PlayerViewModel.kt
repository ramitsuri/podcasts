package com.ramitsuri.podcasts.android.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.PlayerViewState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PlayerViewModel(
    private val playerController: PlayerController,
    private val longLivingScope: CoroutineScope,
    private val settings: Settings,
    private val episodesRepository: EpisodesRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerViewState())
    val state = _state.asStateFlow()

    private var updateEpisodeStateJob: Job? = null
    private var updateSeekJob: Job? = null

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
                    settings.getTrimSilenceFlow(),
                    settings.getSleepTimerFlow(),
                ) { episode, speed, trimSilence, sleepTimer ->
                    EpisodeUpdate(episode, speed, trimSilence, sleepTimer)
                }.collect { (episode, speed, trimSilence, sleepTimer) ->
                    if (episode != null) {
                        _state.update {
                            it.copy(
                                episode = episode,
                                playbackSpeed = speed,
                                sleepTimer = sleepTimer,
                                isCasting = false,
                                trimSilence = trimSilence,
                            )
                        }
                        updateSleepTimerDuration()
                    }
                }
            }
    }

    fun onPlayClicked() {
        longLivingScope.launch {
            val episode = episodesRepository.getCurrentEpisode().firstOrNull()
            if (episode != null) {
                if (episode.isCompleted) {
                    episodesRepository.markNotPlayed(episode.id)
                }
                playerController.play(episode)
            }
        }
    }

    fun onPauseClicked() {
        playerController.pause()
    }

    fun onSkipRequested(by: Duration = 30.seconds) {
        viewModelScope.launch {
            val episode = _state.value.episode
            if (episode == null) {
                LogHelper.v(TAG, "Skip requested but episode is null")
                return@launch
            }
            if (episode.isCompleted) {
                episodesRepository.markNotPlayed(episode.id)
            }
            val newPlayProgress = (episode.progressInSeconds.seconds + by).coerceAtMost((episode.duration ?: 0).seconds)
            episodesRepository.updatePlayProgress(episode.id, newPlayProgress.inWholeSeconds.toInt())
            playerController.seek(newPlayProgress)
            _state.update { it.copy(tempPlayProgress = null) }
        }
    }

    fun onReplayRequested(by: Duration = 10.seconds) {
        viewModelScope.launch {
            val episode = _state.value.episode
            if (episode == null) {
                LogHelper.v(TAG, "Replay requested but episode is null")
                return@launch
            }
            if (episode.isCompleted) {
                episodesRepository.markNotPlayed(episode.id)
            }
            val newPlayProgress = (episode.progressInSeconds.seconds - by).coerceAtLeast(0.seconds)
            episodesRepository.updatePlayProgress(episode.id, newPlayProgress.inWholeSeconds.toInt())
            playerController.seek(newPlayProgress)
            _state.update { it.copy(tempPlayProgress = null) }
        }
    }

    fun onSeekRequested(toPercentOfDuration: Float) {
        _state.update { it.copy(tempPlayProgress = toPercentOfDuration) }
        updateSeekJob?.cancel()
        updateSeekJob =
            viewModelScope.launch {
                delay(300)
                val episode = _state.value.episode
                if (episode == null) {
                    LogHelper.v(TAG, "Seek requested but episode is null")
                    return@launch
                }
                val duration = episode.duration?.seconds
                if (duration == null) {
                    LogHelper.v(TAG, "Seek requested but no duration")
                    return@launch
                }
                if (episode.isCompleted) {
                    episodesRepository.markNotPlayed(episode.id)
                }
                val playProgress = duration.times(toPercentOfDuration.toDouble())
                episodesRepository.updatePlayProgress(episode.id, playProgress.inWholeSeconds.toInt())
                playerController.seek(playProgress)
                _state.update { it.copy(tempPlayProgress = null) }
            }
    }

    fun onSpeedChangeRequested(speed: Float) {
        longLivingScope.launch {
            val rounded =
                speed
                    .times(1000)
                    .roundToInt()
                    .div(1000f)
                    .coerceIn(0.1f, 3.0f)
            settings.setPlaybackSpeed(rounded)
            _state.update { it.copy(playbackSpeed = rounded) }
            // Updated on the player via MediaSessionService
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

    fun toggleTrimSilence() {
        longLivingScope.launch {
            val currentTrimSilence = _state.value.trimSilence
            val newTrimSilence = !currentTrimSilence
            settings.setTrimSilence(newTrimSilence)
            _state.update { it.copy(trimSilence = newTrimSilence) }
            // Updated on the player via MediaSessionService
        }
    }

    fun onSleepTimerRequested(sleepTimer: SleepTimer) {
        viewModelScope.launch {
            settings.setSleepTimer(sleepTimer)
            // State and actual timer get set in the flow collection in updateEpisodeState
        }
    }

    fun onSleepTimerIncreaseRequested() {
        val currentSleepTimer = _state.value.sleepTimer
        if (currentSleepTimer !is SleepTimer.Custom) {
            return
        }
        val newTime = currentSleepTimer.time.plus(2.minutes)
        onSleepTimerRequested(SleepTimer.Custom(time = newTime))
    }

    fun onSleepTimerDecreaseRequested() {
        val currentSleepTimer = _state.value.sleepTimer
        if (currentSleepTimer !is SleepTimer.Custom) {
            return
        }
        val newTime =
            if (currentSleepTimer.time.minus(2.minutes) < clock.now()) {
                clock.now()
            } else {
                currentSleepTimer.time.minus(2.minutes)
            }
        onSleepTimerRequested(SleepTimer.Custom(time = newTime))
    }

    fun onFavoriteClicked() {
        longLivingScope.launch {
            val episodeId = _state.value.episodeId
            if (episodeId != null) {
                episodesRepository.updateFavorite(episodeId, true)
            }
        }
    }

    fun onNotFavoriteClicked() {
        longLivingScope.launch {
            val episodeId = _state.value.episodeId
            if (episodeId != null) {
                episodesRepository.updateFavorite(episodeId, false)
            }
        }
    }

    fun initializePlayer() {
        playerController.initializePlayer()
    }

    private fun updateSleepTimerDuration() {
        when (val sleepTimer = _state.value.sleepTimer) {
            is SleepTimer.Custom -> {
                startCustomSleepTimerDurationUpdate()
                sleepTimer.time.minus(clock.now())
            }

            is SleepTimer.EndOfEpisode -> {
                _state.update { it.copy(sleepTimerDuration = it.remainingDuration) }
            }

            is SleepTimer.None -> {
                _state.update { it.copy(sleepTimerDuration = null) }
            }
        }
    }

    private fun startCustomSleepTimerDurationUpdate() {
        viewModelScope.launch {
            while (true) {
                val sleepTimer = _state.value.sleepTimer
                if (sleepTimer is SleepTimer.Custom) {
                    _state.update { it.copy(sleepTimerDuration = sleepTimer.time.minus(clock.now())) }
                    delay(1_000)
                } else {
                    break
                }
            }
        }
    }

    private data class EpisodeUpdate(
        val episode: Episode?,
        val speed: Float,
        val trimSilence: Boolean,
        val sleepTimer: SleepTimer,
    )

    companion object {
        private const val TAG = "PlayerViewModel"

        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory, KoinComponent {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(
                        playerController = get<PlayerController>(),
                        longLivingScope = get<CoroutineScope>(),
                        settings = get<Settings>(),
                        episodesRepository = get<EpisodesRepository>(),
                        clock = get<Clock>(),
                    ) as T
                }
            }
    }
}
