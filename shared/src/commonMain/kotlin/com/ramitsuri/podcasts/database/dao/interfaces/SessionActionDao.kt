package com.ramitsuri.podcasts.database.dao.interfaces

import com.ramitsuri.podcasts.model.SessionAction

internal interface SessionActionDao {
    suspend fun insert(action: SessionAction)
}
