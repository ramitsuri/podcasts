package com.ramitsuri.podcasts.settings

import kotlinx.coroutines.flow.Flow

internal interface KeyValueStore {
    fun getStringFlow(
        key: Key,
        defaultValue: String?,
    ): Flow<String?>

    suspend fun putString(
        key: Key,
        value: String?,
    )

    fun getFloatFlow(
        key: Key,
        defaultValue: Float?,
    ): Flow<Float?>

    suspend fun putFloat(
        key: Key,
        value: Float?,
    )

    fun getBooleanFlow(
        key: Key,
        defaultValue: Boolean,
    ): Flow<Boolean>

    suspend fun putBoolean(
        key: Key,
        value: Boolean,
    )

    fun getIntFlow(
        key: Key,
        defaultValue: Int,
    ): Flow<Int>

    suspend fun putInt(
        key: Key,
        value: Int,
    )

    suspend fun removeInt(key: Key)
}
