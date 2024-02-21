package com.ramitsuri.podcasts.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CategoriesResponseDto(
    @SerialName("feeds")
    val categories: List<CategoryDto>,
)
