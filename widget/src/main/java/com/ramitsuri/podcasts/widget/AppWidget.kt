package com.ramitsuri.podcasts.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.deepLinkWithArgValue
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.widget.data.WidgetDefinition
import com.ramitsuri.podcasts.widget.data.WidgetState
import com.ramitsuri.podcasts.widget.receiver.WidgetAddedReceiver
import com.ramitsuri.podcasts.widget.receiver.WidgetReceiver
import com.ramitsuri.podcasts.widget.ui.AppWidgetUi

class AppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() =
            SizeMode.Responsive(
                setOf(
                    SMALL_RECTANGLE,
                    BIG_RECTANGLE,
                    BIG_SQUARE,
                    SMALL_SQUARE,
                ),
            )

    override val stateDefinition = WidgetDefinition

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable,
    ) {
        LogHelper.v(TAG, "Composition error: $throwable")
    }

    override suspend fun onDelete(
        context: Context,
        glanceId: GlanceId,
    ) {
        super.onDelete(context, glanceId)
        LogHelper.d(TAG, "Deleted widget $glanceId")
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val state = currentState<WidgetState>()
            AppWidgetUi(state)
        }
    }

    companion object {
        private const val TAG = "AppWidget"
        internal val SMALL_RECTANGLE = DpSize(150.dp, 100.dp)
        internal val BIG_RECTANGLE = DpSize(200.dp, 100.dp)
        internal val BIG_SQUARE = DpSize(200.dp, 200.dp)
        internal val SMALL_SQUARE = DpSize(150.dp, 150.dp)

        suspend fun Context.addWidget() {
            val addedPendingIntent =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(this, WidgetAddedReceiver::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            GlanceAppWidgetManager(applicationContext)
                .requestPinGlanceAppWidget(receiver = WidgetReceiver::class.java, successCallback = addedPendingIntent)
        }

        suspend fun Context.updateWidget(
            episode: Episode,
            playing: Boolean,
        ) {
            val appWidgetManager = GlanceAppWidgetManager(this)
            val url =
                Route.EpisodeDetails(
                    episodeId = episode.id,
                    podcastId = episode.podcastId,
                ).deepLinkWithArgValue ?: return
            appWidgetManager.getGlanceIds(AppWidget::class.java).forEach { glanceId ->
                updateAppWidgetState(
                    context = this,
                    definition = WidgetDefinition,
                    glanceId = glanceId,
                    updateState = {
                        WidgetState.CurrentlyPlaying(
                            episodeTitle = episode.title,
                            isPlaying = playing,
                            albumArtUri = episode.podcastImageUrl,
                            deepLinkUrl = url,
                        )
                    },
                )
                AppWidget().update(this, glanceId)
            }
        }
    }
}
