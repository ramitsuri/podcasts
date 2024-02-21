package com.ramitsuri.podcasts.network.model

import com.ramitsuri.podcasts.network.serialization.CategoriesDeserializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PodcastDto(
    @SerialName("id")
    val id: Int,

    @SerialName("podcastGuid")
    val guid: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("author")
    val author: String,

    @SerialName("ownerName")
    val owner: String,

    @SerialName("url")
    val url: String,

    @SerialName("link")
    val link: String,

    @SerialName("image")
    val image: String,

    @SerialName("artwork")
    val artwork: String,

    @SerialName("explicit")
    val explicit: Boolean,

    @SerialName("episodeCount")
    val episodeCount: Int,

    @Serializable(with = CategoriesDeserializer::class)
    @SerialName("categories")
    val categories: List<CategoryDto>,
)
