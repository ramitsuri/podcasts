package com.ramitsuri.podcasts.android

import android.app.Application
import com.ramitsuri.podcasts.AppInfo
import com.ramitsuri.podcasts.initKoin
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
    }

    private fun initDependencyInjection() {
        initKoin(
            module {
                single<Application> {
                    this@MainApplication
                }

                factory<AppInfo> {
                    AndroidAppInfo()
                }
            },
        )
    }
}
