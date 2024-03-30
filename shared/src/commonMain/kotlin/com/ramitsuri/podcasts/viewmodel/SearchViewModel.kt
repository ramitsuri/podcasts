package com.ramitsuri.podcasts.viewmodel

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.model.ui.SearchResult
import com.ramitsuri.podcasts.model.ui.SearchViewState
import com.ramitsuri.podcasts.network.model.SearchPodcastsRequest
import com.ramitsuri.podcasts.repositories.PodcastsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val podcastsRepository: PodcastsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchViewState())
    val state = _state.asStateFlow()

    fun onSearchTermUpdated(searchTerm: String) {
        _state.update {
            it.copy(term = searchTerm)
        }
    }

    fun clearSearch() {
        _state.update {
            it.copy(term = "", result = SearchResult.Default)
        }
    }

    fun search() {
        _state.update { it.copy(result = SearchResult.Searching) }
        viewModelScope.launch {
            val term = _state.value.term
            println("Searching with $term")
            val result = podcastsRepository.search(
                request = SearchPodcastsRequest(
                    term = term,
                    findSimilar = true,
                    maxResults = 20,
                ),
            )

            when (result) {
                is PodcastResult.Failure -> {
                    _state.update { it.copy(result = SearchResult.Error) }
                }

                is PodcastResult.Success -> {
                    _state.update { it.copy(result = SearchResult.Success(podcasts = result.data)) }
                }
            }
        }
    }
}
