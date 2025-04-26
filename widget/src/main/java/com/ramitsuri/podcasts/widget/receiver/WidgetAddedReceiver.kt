package com.ramitsuri.podcasts.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.widget.AppWidget.Companion.updateWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetAddedReceiver : BroadcastReceiver(), KoinComponent {
    private val coroutineScope by inject<CoroutineScope>()
    private val episodesRepository by inject<EpisodesRepository>()
    private val settings by inject<Settings>()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        LogHelper.d(TAG, "Widget added")
        coroutineScope.launch {
            val episode = episodesRepository.getCurrentEpisode().first()
            if (episode == null) {
                return@launch
            }
            val isPlaying = settings.getPlayingStateFlow().first() == PlayingState.PLAYING
            context.updateWidget(episode, isPlaying)
        }
    }

    companion object {
        private const val TAG = "WidgetAddedReceiver"
    }
}
