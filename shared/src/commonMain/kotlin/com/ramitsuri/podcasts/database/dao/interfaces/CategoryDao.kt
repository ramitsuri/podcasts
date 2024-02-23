package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.CategoryEntity
import com.ramitsuri.podcasts.model.Category

internal interface CategoryDao {
    suspend fun get(): List<CategoryEntity>

    suspend fun get(ids: List<Int>): List<CategoryEntity>

    suspend fun deleteCategories()

    suspend fun insertCategories(categories: List<Category>)
}
