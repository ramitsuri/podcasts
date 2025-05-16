package com.ramitsuri.podcasts.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

internal class DataStoreKeyValueStore(
    private val dataStore: DataStore<Preferences>,
) : KeyValueStore {
    override fun getStringFlow(
        key: Key,
        defaultValue: String?,
    ): Flow<String?> {
        return dataStore
            .data
            .mapDistinct {
                it[stringPreferencesKey(key.value)] ?: defaultValue
            }
    }

    override suspend fun putString(
        key: Key,
        value: String?,
    ) {
        if (value == null) {
            remove(stringPreferencesKey(key.value))
        } else {
            dataStore.edit {
                it[stringPreferencesKey(key.value)] = value
            }
        }
    }

    override fun getFloatFlow(
        key: Key,
        defaultValue: Float?,
    ): Flow<Float?> {
        return dataStore
            .data
            .mapDistinct {
                it[floatPreferencesKey(key.value)] ?: defaultValue
            }
    }

    override suspend fun putFloat(
        key: Key,
        value: Float?,
    ) {
        if (value == null) {
            remove(floatPreferencesKey(key.value))
        } else {
            dataStore.edit {
                it[floatPreferencesKey(key.value)] = value
            }
        }
    }

    override fun getBooleanFlow(
        key: Key,
        defaultValue: Boolean,
    ): Flow<Boolean> {
        return dataStore
            .data
            .mapDistinct {
                it[booleanPreferencesKey(key.value)] ?: defaultValue
            }
    }

    override suspend fun putBoolean(
        key: Key,
        value: Boolean,
    ) {
        dataStore.edit {
            it[booleanPreferencesKey(key.value)] = value
        }
    }

    override fun getIntFlow(
        key: Key,
        defaultValue: Int,
    ): Flow<Int> {
        return dataStore
            .data
            .mapDistinct {
                it[intPreferencesKey(key.value)] ?: defaultValue
            }
    }

    override suspend fun putInt(
        key: Key,
        value: Int,
    ) {
        dataStore.edit {
            it[intPreferencesKey(key.value)] = value
        }
    }

    override suspend fun removeInt(key: Key) {
        remove(intPreferencesKey(key.value))
    }

    override suspend fun getAll(): Map<String, Any> {
        return dataStore.data.firstOrNull()?.asMap()?.mapKeys { it.key.name } ?: mapOf()
    }

    override suspend fun removeAll() {
        dataStore.edit {
            it.clear()
        }
    }

    private suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit {
            it.remove(key)
        }
    }

    private inline fun <T, R> Flow<T>.mapDistinct(crossinline transform: suspend (value: T) -> R) =
        map(transform).distinctUntilChanged()
}
