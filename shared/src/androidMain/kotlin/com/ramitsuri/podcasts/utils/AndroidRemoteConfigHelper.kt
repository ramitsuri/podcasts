package com.ramitsuri.podcasts.utils

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.ramitsuri.podcasts.settings.Settings
import kotlinx.coroutines.tasks.await

internal class AndroidRemoteConfigHelper(
    private val settings: Settings,
) : RemoteConfigHelper {
    override suspend fun initialize() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        val fetched = remoteConfig.fetchAndActivate().await()
        LogHelper.v(TAG, "Remote config fetched: $fetched")
    }

    override suspend fun isDevicePrivileged(): Boolean {
        val deviceId = settings.getDeviceId()
        val privilegedIds =
            Firebase
                .remoteConfig
                .getString(RemoteConfigHelper.KEY_PRIVILEGED_IDS)
                .split(",")
                .filter { it.isNotBlank() }
        return deviceId in privilegedIds
    }

    companion object {
        private const val TAG = "AndroidRemoteConfigHelper"
    }
}
