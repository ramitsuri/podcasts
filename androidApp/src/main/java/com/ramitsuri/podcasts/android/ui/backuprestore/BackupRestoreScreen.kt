package com.ramitsuri.podcasts.android.ui.backuprestore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    state: BackupRestoreViewState,
    onBack: () -> Unit,
    onBackupFilePicked: (Uri) -> Unit,
    onRestoreFilePicked: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
        modifier
            .fillMaxSize(),
    ) {
        TopAppBar(onBack = onBack)
        Column(
            modifier =
            modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BackupButton(
                suggestedFileName = state.backupSuggestedFileName,
                mimeType = state.mimeType,
                onBackupFilePicked = onBackupFilePicked,
            )
            RestoreButton(
                mimeType = state.mimeType,
                onRestoreFilePicked = onRestoreFilePicked,
            )
        }
    }
}

@Composable
private fun BackupButton(
    suggestedFileName: String,
    mimeType: String,
    onBackupFilePicked: (Uri) -> Unit
) {
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(mimeType),
            onResult = { uri ->
                if (uri != null) {
                    onBackupFilePicked(uri)
                }
            },
        )
    FilledTonalButton(onClick = { filePicker.launch(suggestedFileName) }) {
        Text(text = stringResource(id = R.string.backup_restore_backup))
    }
}

@Composable
private fun RestoreButton(
    mimeType: String,
    onRestoreFilePicked: (Uri) -> Unit
) {
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri != null) {
                    onRestoreFilePicked(uri)
                }
            },
        )
    FilledTonalButton(onClick = { filePicker.launch(mimeType) }) {
        Text(text = stringResource(id = R.string.backup_restore_restore))
    }
}
