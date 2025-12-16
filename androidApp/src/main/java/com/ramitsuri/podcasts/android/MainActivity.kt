package com.ramitsuri.podcasts.android

import android.app.assist.AssistContent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.NavKey
import com.ramitsuri.podcasts.android.navigation.NavGraph
import com.ramitsuri.podcasts.android.navigation.Navigator
import com.ramitsuri.podcasts.android.navigation.deeplink.DeepLinkMatcher
import com.ramitsuri.podcasts.android.navigation.deeplink.DeepLinkPattern
import com.ramitsuri.podcasts.android.navigation.deeplink.DeepLinkRequest
import com.ramitsuri.podcasts.android.navigation.deeplink.KeyDecoder
import com.ramitsuri.podcasts.android.navigation.rememberNavigationState
import com.ramitsuri.podcasts.android.ui.AppTheme
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.deepLinkWithArgName
import com.ramitsuri.podcasts.navigation.deepLinkWithArgValue

class MainActivity : ComponentActivity() {
    private lateinit var navigator: Navigator
    private val deepLinkPatterns: List<DeepLinkPattern<out NavKey>> = listOf(
        DeepLinkPattern(Route.EpisodeDetails.serializer(), (Route.EpisodeDetails.deepLinkWithArgName.toUri()))
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val uri: Uri? = intent.data
        // associate the target with the correct backstack key
        val key: NavKey = uri?.let {
            /** STEP 2. Parse requested deeplink */
            val request = DeepLinkRequest(uri)
            /** STEP 3. Compared requested with supported deeplink to find match*/
            val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
                DeepLinkMatcher(request, pattern).match()
            }
            /** STEP 4. If match is found, associate match to the correct key*/
            match?.let {
                //leverage kotlinx.serialization's Decoder to decode
                // match result into a backstack key
                KeyDecoder(match.args)
                    .decodeSerializableValue(match.serializer)
            }
        } ?: Route.Home

        setContent {
            val darkTheme = isSystemInDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle =
                        SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { darkTheme },
                    navigationBarStyle =
                        SystemBarStyle.auto(
                            lightScrim,
                            darkScrim,
                        ) { darkTheme },
                )
                onDispose {}
            }
            val navigationState = rememberNavigationState(
                startRoute = Route.Home,
                topLevelRoutes = setOf(Route.Home, Route.Explore, Route.Library),
            )

            navigator = remember { Navigator(navigationState) }

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavGraph(navigator = navigator)
                }
            }
        }
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)
        val current = navigator.currentDestination as? Route.EpisodeDetails
        outContent.webUri = current?.deepLinkWithArgValue?.toUri()
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:
 * activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;
 * drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
