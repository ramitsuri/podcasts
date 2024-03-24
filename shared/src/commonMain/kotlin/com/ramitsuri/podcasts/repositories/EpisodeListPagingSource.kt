package com.ramitsuri.podcasts.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ramitsuri.podcasts.database.dao.interfaces.EpisodesDao
import com.ramitsuri.podcasts.model.Episode

internal class EpisodeListPagingSource(private val episodesDao: EpisodesDao) : PagingSource<Int, Episode>() {
    override fun getRefreshKey(state: PagingState<Int, Episode>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Episode> {
        return try {
            val page = params.key ?: 1
            val response = episodesDao.getSubscribedEpisodes(page = page).map { Episode(it) }

            LoadResult.Page(
                data = response,
                prevKey = if (page == 1) null else page.minus(1),
                nextKey = if (response.isEmpty()) null else page.plus(1),
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
