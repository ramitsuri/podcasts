package com.ramitsuri.podcasts.network.api.interfaces

import com.ramitsuri.podcasts.model.PodcastResult
import com.ramitsuri.podcasts.network.model.CategoriesResponseDto

internal interface CategoriesApi {
    suspend fun get(): PodcastResult<CategoriesResponseDto>
}
