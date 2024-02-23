package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.network.model.CategoryDto

data class Category(val id: Int, val name: String) {
    internal constructor(dto: CategoryDto) : this(
        id = dto.id,
        name = dto.name,
    )
}
