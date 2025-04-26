package com.ramitsuri.podcasts.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class WidgetAction : ActionCallback, KoinComponent {
    private val controller by inject<PlayerController>()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val actionType = parameters[ActionParameters.Key<ActionType>(ACTION_TYPE)]
        if (actionType == null) {
            LogHelper.v(TAG, "Unable to find action type")
            return
        }
        withContext(Dispatchers.Main) {
            when (actionType) {
                ActionType.PLAY -> {
                    controller.playCurrentEpisode()
                }

                ActionType.PAUSE -> {
                    controller.pause()
                }

                ActionType.SKIP -> {
                    controller.skip()
                }

                ActionType.REPLAY -> {
                    controller.replay()
                }
            }
        }
    }

    companion object : KoinComponent {
        private const val TAG = "WidgetAction"
        private const val ACTION_TYPE = "action_type"

        private enum class ActionType {
            PLAY,
            PAUSE,
            SKIP,
            REPLAY,
        }

        fun play() =
            actionRunCallback<WidgetAction>(
                actionParametersOf(
                    ActionParameters.Key<ActionType>(ACTION_TYPE) to ActionType.PLAY,
                ),
            )

        fun pause() =
            actionRunCallback<WidgetAction>(
                actionParametersOf(
                    ActionParameters.Key<ActionType>(ACTION_TYPE) to ActionType.PAUSE,
                ),
            )

        fun skip() =
            actionRunCallback<WidgetAction>(
                actionParametersOf(
                    ActionParameters.Key<ActionType>(ACTION_TYPE) to ActionType.SKIP,
                ),
            )

        fun replay() =
            actionRunCallback<WidgetAction>(
                actionParametersOf(
                    ActionParameters.Key<ActionType>(ACTION_TYPE) to ActionType.REPLAY,
                ),
            )
    }
}
