package com.ramitsuri.podcasts

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.podcasts.utils.DispatcherProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache5.Apache5
import org.koin.dsl.module

actual val platformModule =
    module {
        single<HttpClientEngine> {
            Apache5.create()
        }

        single<SqlDriver> {
            JdbcSqliteDriver("jdbc:sqlite:test.db")
        }

        single<DispatcherProvider> {
            DispatcherProvider()
        }
    }
