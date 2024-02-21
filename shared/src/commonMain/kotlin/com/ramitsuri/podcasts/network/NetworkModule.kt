package com.ramitsuri.podcasts.network

import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.ramitsuri.podcasts.build.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.sha1
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal fun provideHttpClient(
    isDebug: Boolean,
    podcastIndexKey: String = BuildKonfig.PODCAST_INDEX_KEY,
    podcastIndexSecret: String = BuildKonfig.PODCAST_INDEX_SECRET,
    clock: Clock = Clock.System,
    clientEngine: HttpClientEngine,
): HttpClient {
    return HttpClient(clientEngine) {
        val log =
            KermitLogger(
                loggerConfigInit(platformLogWriter()),
                "Http",
            )
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                },
            )
        }

        install(Logging) {
            logger =
                object : KtorLogger {
                    override fun log(message: String) {
                        log.v { message }
                    }
                }
            level = if (isDebug) LogLevel.ALL else LogLevel.NONE
        }

        install(DefaultRequest) {
            val secondsSinceEpoch = clock.now().epochSeconds
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("User-Agent", "Podcasts")
            header("X-Auth-Date", secondsSinceEpoch)
            header("X-Auth-Key", podcastIndexKey)
            header(
                "Authorization",
                authHash(
                    apiKey = podcastIndexKey,
                    apiSecret = podcastIndexSecret,
                    secondsSinceEpoch = secondsSinceEpoch,
                ),
            )
        }

        install(HttpRequestRetry) {
            retryIf(3) { _, httpResponse ->
                when {
                    httpResponse.status.value in 500..599 -> true
                    httpResponse.status == HttpStatusCode.TooManyRequests -> true
                    else -> false
                }
            }
            constantDelay()
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun authHash(
    apiKey: String,
    apiSecret: String,
    secondsSinceEpoch: Long,
): String {
    // A SHA-1 hash of the X-Auth-Key, the corresponding secret and the X-Auth-Date value concatenated as a string.
    // The resulting hash should be encoded as a hexadecimal value, two digits per byte, using lower case letters
    // for the hex digits "a" through "f".
    return sha1("$apiKey$apiSecret$secondsSinceEpoch".toByteArray())
        .asUByteArray()
        .joinToString("") {
            it.toString(16).padStart(2, '0')
        }
}
