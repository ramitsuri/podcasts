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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

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
    private var updateQueueJob: Job? = null

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
                                playbackSpeed = speed.times(10).roundToInt(),
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
        playerController.playCurrentEpisode()
    }

    fun onPauseClicked() {
        playerController.pause()
    }

    fun onSkipRequested() {
        viewModelScope.launch {
            playerController.skip()
            _state.update { it.copy(tempPlayProgress = null) }
        }
    }

    fun onReplayRequested() {
        viewModelScope.launch {
            playerController.replay()
            _state.update { it.copy(tempPlayProgress = null) }
        }
    }

    fun onSeekRequested(toPercentOfDuration: Float) {
        _state.update { it.copy(tempPlayProgress = toPercentOfDuration) }
        updateSeekJob?.cancel()
        updateSeekJob =
            viewModelScope.launch {
                delay(300)
                playerController.seek(toPercentOfDuration)
                _state.update { it.copy(tempPlayProgress = null) }
            }
    }

    // Speed is 10x the actual value so that it can be represented as integer
    fun onSpeedChangeRequested(speed: Int) {
        longLivingScope.launch {
            val rounded =
                speed
                    .div(10f)
                    .coerceIn(0.1f, 3.0f)
            settings.setPlaybackSpeed(rounded)
            _state.update { it.copy(playbackSpeed = speed) }
            // Updated on the player via MediaSessionService
        }
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

    fun onSleepTimerCancelRequested() {
        viewModelScope.launch {
            settings.setSleepTimer(SleepTimer.None)
        }
    }

    fun onSleepTimerEndOfEpisodeRequested() {
        viewModelScope.launch {
            settings.setSleepTimer(SleepTimer.EndOfEpisode)
        }
    }

    fun onSleepTimerCustomRequested(minutes: Int) {
        viewModelScope.launch {
            settings.setSleepTimer(SleepTimer.Custom(clock.now().plus(minutes.minutes)))
        }
    }

    fun onSleepTimerIncreaseRequested() {
        val currentSleepTimer = _state.value.sleepTimer
        if (currentSleepTimer !is SleepTimer.Custom) {
            return
        }
        val newTime = currentSleepTimer.time.plus(5.minutes)
        viewModelScope.launch {
            settings.setSleepTimer(SleepTimer.Custom(time = newTime))
        }
    }

    fun onSleepTimerDecreaseRequested() {
        val currentSleepTimer = _state.value.sleepTimer
        if (currentSleepTimer !is SleepTimer.Custom) {
            return
        }
        val newTime =
            if (currentSleepTimer.time.minus(5.minutes) < clock.now()) {
                clock.now()
            } else {
                currentSleepTimer.time.minus(5.minutes)
            }
        viewModelScope.launch {
            settings.setSleepTimer(SleepTimer.Custom(time = newTime))
        }
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

    fun viewStarted() {
        startUpdatingQueue()
    }

    fun viewStopped() {
        stopUpdatingQueue()
    }

    private fun startUpdatingQueue() {
        updateQueueJob =
            longLivingScope.launch {
                launch {
                    settings
                        .autoPlayNextInQueue()
                        .collect {
                            playerController.updateQueue()
                        }
                }
                launch {
                    settings
                        .getSleepTimerFlow()
                        .filter { it is SleepTimer.EndOfEpisode }
                        .collect {
                            playerController.updateQueue()
                        }
                }
            }
    }

    private fun stopUpdatingQueue() {
        updateQueueJob?.cancel()
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
