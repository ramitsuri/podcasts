package com.ramitsuri.podcasts.settings

import kotlinx.coroutines.flow.Flow

internal interface KeyValueStore {
    suspend fun getString(
        key: Key,
        defaultValue: String?,
    ): String?

    fun getStringFlow(
        key: Key,
        defaultValue: String?,
    ): Flow<String?>

    suspend fun putString(
        key: Key,
        value: String?,
    )
}
