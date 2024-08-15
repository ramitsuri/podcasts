package com.ramitsuri.podcasts.utils

import com.ramitsuri.podcasts.player.PlayerController
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QueueRearrangementHelper(
    scope: CoroutineScope,
    repo: EpisodesRepository,
    playerController: PlayerController,
) {
    private val _queuePositions = MutableStateFlow<Map<String, Int>>(mapOf())
    val queuePositions = _queuePositions.asStateFlow()

    private val queueRearrangementChannel = Channel<Positions>()

    suspend fun updateQueuePositions(
        id1: String,
        position1: Int,
        id2: String,
        position2: Int,
    ) {
        _queuePositions.update {
            it.toMutableMap().apply {
                put(id1, position1)
                put(id2, position2)
            }
        }
        queueRearrangementChannel.send(Positions(id1, position1, id2, position2))
    }

    init {
        scope.launch {
            queueRearrangementChannel.consumeEach { positions ->
                repo.updateQueuePositions(positions.id1, positions.position1, positions.id2, positions.position2)
                playerController.updateQueue()
            }
        }
    }

    private data class Positions(
        val id1: String,
        val position1: Int,
        val id2: String,
        val position2: Int,
    )
}
