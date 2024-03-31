package com.ramitsuri.podcasts.model.ui

import com.ramitsuri.podcasts.model.Podcast

data class SearchViewState(
    val term: String = "",
    val result: SearchResult = SearchResult.Default,
)

sealed interface SearchResult {
    data object Default : SearchResult

    data object Searching : SearchResult

    data class Success(val podcasts: List<Podcast>) : SearchResult

    data object Error : SearchResult
}
