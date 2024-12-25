package com.ramitsuri.podcasts.android.ui.backuprestore

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ramitsuri.podcasts.repositories.BackupRestoreRepository
import com.ramitsuri.podcasts.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.IOException

class BackupRestoreViewModel(
    application: Application,
    private val backupRestoreRepository: BackupRestoreRepository,
) : AndroidViewModel(application) {
    val state =
        MutableStateFlow(
            BackupRestoreViewState(
                backupSuggestedFileName = BACKUP_FILE_NAME,
                mimeType = MIME_TYPE,
            ),
        ).asStateFlow()

    fun onBackupFilePicked(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>()
                .contentResolver
                .openOutputStream(uri)
                .use { outputStream ->
                    if (outputStream != null) {
                        try {
                            val backupData = backupRestoreRepository.getBackupData()
                            outputStream.write(backupData)
                        } catch (e: IOException) {
                            LogHelper.v(TAG, "Error writing backup file: ${e.message}")
                        }
                    }
                }
        }
    }

    fun onRestoreFilePicked(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>()
                .contentResolver
                .openInputStream(uri)
                .use { inputStream ->
                    if (inputStream != null) {
                        try {
                            val backupData = inputStream.readBytes()
                            backupRestoreRepository.restoreBackupData(backupData)
                        } catch (e: IOException) {
                            LogHelper.v(TAG, "Error reading backup file: ${e.message}")
                        }
                    }
                }
        }
    }

    companion object {
        private const val TAG = "BackupRestoreViewModel"
        private const val BACKUP_FILE_NAME = "podcasts_backup.json"
        private const val MIME_TYPE = "application/json"

        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory, KoinComponent {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BackupRestoreViewModel(
                        application = get(),
                        backupRestoreRepository = get(),
                    ) as T
                }
            }
    }
}
