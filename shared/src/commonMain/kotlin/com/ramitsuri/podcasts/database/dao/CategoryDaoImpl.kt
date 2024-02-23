package com.ramitsuri.podcasts.database.dao

import com.ramitsuri.podcasts.CategoryEntity
import com.ramitsuri.podcasts.CategoryEntityQueries
import com.ramitsuri.podcasts.database.dao.interfaces.CategoryDao
import com.ramitsuri.podcasts.model.Category
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class CategoryDaoImpl(
    private val categoryEntityQueries: CategoryEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : CategoryDao {
    override suspend fun get(): List<CategoryEntity> {
        return withContext(ioDispatcher) {
            categoryEntityQueries
                .getCategories()
                .executeAsList()
        }
    }

    override suspend fun get(ids: List<Int>): List<CategoryEntity> {
        return withContext(ioDispatcher) {
            categoryEntityQueries
                .getCategoriesForIds(ids)
                .executeAsList()
        }
    }

    override suspend fun deleteCategories() {
        withContext(ioDispatcher) {
            categoryEntityQueries.deleteCategories()
        }
    }

    override suspend fun insertCategories(categories: List<Category>) {
        withContext(ioDispatcher) {
            categories.forEach { insert(it) }
        }
    }

    private fun insert(category: Category) {
        categoryEntityQueries.insertCategory(
            CategoryEntity(
                id = category.id,
                name = category.name,
            ),
        )
    }
}
