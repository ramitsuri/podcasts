package com.ramitsuri.podcasts.android.ui.importsub

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.repositories.PodcastsAndEpisodesRepository
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class ImportSubscriptionsViewModel(
    application: Application,
    private val podcastsAndEpisodesRepository: PodcastsAndEpisodesRepository,
    private val podcastsRepository: PodcastsRepository,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ImportSubscriptionsViewState())
    val state: StateFlow<ImportSubscriptionsViewState> = _state

    fun onSubscriptionDataFilePicked(uri: Uri) {
        getApplication<Application>()
            .contentResolver
            .openInputStream(uri)
            .use { inputStream ->
                if (inputStream != null) {
                    val subscriptionDataList = parseInputStream(inputStream)
                    onSubscriptionDataListReceived(subscriptionDataList)
                }
            }
    }

    private fun onSubscriptionDataListReceived(subscriptionDataList: List<SubscriptionData>) {
        viewModelScope.launch {
            val imported = mutableListOf<Podcast>()
            val failedWithSuggestion = mutableListOf<FailedToImportWithSuggestion>()
            val failed = mutableListOf<String>()
            subscriptionDataList.map {
                launch {
                    val (importedByUrl, importedByName) =
                        podcastsRepository.getPodcastByUrlOrName(
                            url = it.xmlUrl,
                            name = it.text,
                        )
                    if (importedByUrl != null) {
                        imported.add(importedByUrl)
                    } else if (importedByName != null) {
                        failedWithSuggestion.add(
                            FailedToImportWithSuggestion(
                                text = it.text,
                                suggestion = importedByName,
                            ),
                        )
                    } else {
                        failed.add(it.text)
                    }
                }
            }.joinAll()
            _state.update {
                it.copy(
                    isLoading = false,
                    failedToImportWithSuggestion = failedWithSuggestion,
                    imported = imported,
                    failedToImport = failed,
                )
            }
        }
    }

    fun onSuggestionAccepted(failedToImport: FailedToImportWithSuggestion) {
        val currentImported = _state.value.imported.toMutableList()
        val currentFailed = _state.value.failedToImportWithSuggestion.toMutableList()
        _state.update {
            it.copy(
                imported = currentImported.plus(failedToImport.suggestion),
                failedToImportWithSuggestion = currentFailed.minus(failedToImport),
            )
        }
    }

    fun subscribeAllPodcasts() {
        val podcasts = _state.value.imported
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            podcastsAndEpisodesRepository.subscribeToImportedPodcasts(podcasts)
            _state.update {
                it.copy(isLoading = false, subscribed = true)
            }
        }
    }

    private fun parseInputStream(inputStream: InputStream): List<SubscriptionData> {
        val subscriptionDataList = mutableListOf<SubscriptionData>()

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.name == "outline") {
                    val text = parser.getAttributeValue(null, "text")
                    val xmlUrl = parser.getAttributeValue(null, "xmlUrl")

                    if (text != null && xmlUrl != null) {
                        subscriptionDataList.add(SubscriptionData(text, xmlUrl))
                    }
                }
            }
            event = parser.next()
        }
        return subscriptionDataList
    }

    private data class SubscriptionData(val text: String, val xmlUrl: String)

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory, KoinComponent {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ImportSubscriptionsViewModel(
                        application = get(),
                        podcastsAndEpisodesRepository = get(),
                        podcastsRepository = get(),
                    ) as T
                }
            }
    }
}
