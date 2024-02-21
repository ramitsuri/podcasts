package com.ramitsuri.podcasts.network.model

import com.ramitsuri.podcasts.network.serialization.CategoriesDeserializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TrendingPodcastDto(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("author")
    val author: String,

    @SerialName("url")
    val url: String,

    @SerialName("image")
    val image: String,

    @SerialName("artwork")
    val artwork: String,

    @SerialName("trendScore")
    val trendScore: Int,

    @Serializable(with = CategoriesDeserializer::class)
    @SerialName("categories")
    val categories: List<CategoryDto>,
)
