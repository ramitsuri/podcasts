package com.ramitsuri.podcasts.android.ui.importsub

import com.ramitsuri.podcasts.model.Podcast

data class ImportSubscriptionsViewState(
    val isLoading: Boolean = true,
    val subscribed: Boolean = false,
    val failure: Boolean = false,
    val imported: List<Podcast> = listOf(),
    val failedToImportWithSuggestion: List<FailedToImportWithSuggestion> = listOf(),
    val failedToImport: List<String> = listOf(),
)

data class FailedToImportWithSuggestion(val text: String, val suggestion: Podcast)
