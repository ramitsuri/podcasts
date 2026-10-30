package com.ramitsuri.podcasts.android

import com.ramitsuri.podcasts.AppInfo
import com.ramitsuri.podcasts.android.BuildConfig

class AndroidAppInfo : AppInfo {
    override val isDebug: Boolean = BuildConfig.DEBUG
}
