package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.TrendingPodcastEntity
import com.ramitsuri.podcasts.network.model.TrendingPodcastDto

data class TrendingPodcast(
    val id: Long,
    val title: String,
    val description: String,
    val author: String,
    val url: String,
    val image: String,
    val artwork: String,
    val trendScore: Long,
    val categories: List<Category>,
) {
    internal constructor(dto: TrendingPodcastDto) : this(
        id = dto.id,
        title = dto.title,
        description = dto.description,
        author = dto.author,
        url = dto.url,
        image = dto.image,
        artwork = dto.artwork,
        trendScore = dto.trendScore,
        categories = dto.categories.map { Category(it) },
    )

    internal constructor(
        entity: TrendingPodcastEntity,
        categories: List<Category>,
    ) : this(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        author = entity.author,
        url = entity.url,
        image = entity.image,
        artwork = entity.artwork,
        trendScore = entity.trendScore,
        categories = categories,
    )
}
