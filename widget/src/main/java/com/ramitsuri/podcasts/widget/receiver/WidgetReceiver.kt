package com.ramitsuri.podcasts.widget.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.settings.Settings
import com.ramitsuri.podcasts.widget.AppWidget
import com.ramitsuri.podcasts.widget.AppWidget.Companion.updateWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    private val coroutineScope by inject<CoroutineScope>()
    private val episodesRepository by inject<EpisodesRepository>()
    private val settings by inject<Settings>()

    override val glanceAppWidget: GlanceAppWidget
        get() = AppWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        coroutineScope.launch {
            val episode = episodesRepository.getCurrentEpisode().firstOrNull() ?: return@launch
            val isPlaying = settings.getPlayingStateFlow().firstOrNull() == PlayingState.PLAYING
            context.updateWidget(episode, isPlaying)
        }
    }
}
