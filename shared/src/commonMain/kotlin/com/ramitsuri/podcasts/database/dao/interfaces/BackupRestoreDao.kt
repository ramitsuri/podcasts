package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.model.BackupData

interface BackupRestoreDao {
    suspend fun getData(): BackupData

    suspend fun removeData()

    suspend fun addData(data: BackupData)
}
