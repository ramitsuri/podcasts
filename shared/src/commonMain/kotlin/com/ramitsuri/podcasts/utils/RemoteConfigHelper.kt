package com.ramitsuri.podcasts.utils

interface RemoteConfigHelper {
    suspend fun initialize()

    suspend fun isDevicePrivileged(): Boolean

    companion object {
        const val KEY_PRIVILEGED_IDS = "PRIVILEGED_IDS"
    }
}
