package com.ramitsuri.podcasts.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CategoryDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
)
