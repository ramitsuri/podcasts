package com.ramitsuri.podcasts.repositories

import com.ramitsuri.podcasts.database.dao.interfaces.BackupRestoreDao
import com.ramitsuri.podcasts.model.BackupData
import com.ramitsuri.podcasts.model.fromMap
import com.ramitsuri.podcasts.settings.Key
import com.ramitsuri.podcasts.settings.KeyValueStore
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class BackupRestoreRepository internal constructor(
    private val backupRestoreDao: BackupRestoreDao,
    private val json: Json,
    private val keyValueStore: KeyValueStore,
) {
    suspend fun getBackupData(): ByteArray {
        val dbData = backupRestoreDao.getData()
        val settingsData = keyValueStore.getAll()
        val data = dbData.copy(prefs = settingsData.fromMap())
        val jsonString = json.encodeToString(BackupData.serializer(), data)
        return jsonString.toByteArray()
    }

    suspend fun restoreBackupData(bytes: ByteArray) =
        coroutineScope {
            val jsonString = bytes.decodeToString()
            val data = runCatching { json.decodeFromString<BackupData>(jsonString) }.getOrNull()
            if (data == null) {
                return@coroutineScope
            }
            // Restore database
            backupRestoreDao.removeData()
            backupRestoreDao.addData(data)

            // Restore prefs
            keyValueStore.removeAll()
            data.prefs.forEach { (key, value, type) ->
                val prefKey = Key.fromStringKey(key) ?: return@forEach
                when (type) {
                    BackupData.STRING -> {
                        keyValueStore.putString(prefKey, value)
                    }

                    BackupData.FLOAT -> {
                        val floatValue = value.toFloatOrNull() ?: return@forEach
                        keyValueStore.putFloat(prefKey, floatValue)
                    }

                    BackupData.BOOL -> {
                        val booleanValue = value.toBooleanStrictOrNull() ?: return@forEach
                        keyValueStore.putBoolean(prefKey, booleanValue)
                    }

                    BackupData.INT -> {
                        val intValue = value.toIntOrNull() ?: return@forEach
                        keyValueStore.putInt(prefKey, intValue)
                    }
                }
            }
        }
}
