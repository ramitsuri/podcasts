package com.ramitsuri.podcasts.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width

class AppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Responsive(
            setOf(
                SMALL_SQUARE,
                HORIZONTAL_RECTANGLE,
                BIG_SQUARE,
            ),
        )

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val testState = AppWidgetViewState(
            episodeTitle =
                "100 - Android 15 DP 1, Stable Studio Iguana, Cloud Photo Picker, and more!",
            podcastTitle = "Now in Android",
            isPlaying = false,
            albumArtUri = "https://static.libsyn.com/p/assets/9/f/f/3/" +
                "9ff3cb5dc6cfb3e2e5bbc093207a2619/NIA000_PodcastThumbnail.png",
        )

        provideContent {
            val size = LocalSize.current
            val playPauseIcon = if (testState.isPlaying) PlayPauseIcon.Pause else PlayPauseIcon.Play
            val artUri = testState.albumArtUri.toUri()

            GlanceTheme {
                Scaffold {
                    Column(
                        GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when {
                            size.height >= BIG_SQUARE.height && size.width >= BIG_SQUARE.width -> {
                                AlbumArt(artUri, GlanceModifier.size(BIG_SQUARE.width))
                                Spacer(GlanceModifier.height(24.dp))
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    PlayPauseButton(modifier = GlanceModifier.size(64.dp), state = playPauseIcon) { }
                                }
                            }

                            size.height >= SMALL_SQUARE.height && size.width >= SMALL_SQUARE.width -> {
                                AlbumArt(artUri, GlanceModifier.size(SMALL_SQUARE.width))
                                Spacer(GlanceModifier.height(8.dp))
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    PlayPauseButton(modifier = GlanceModifier.size(64.dp), state = playPauseIcon) { }
                                }
                            }

                            size.height >= HORIZONTAL_RECTANGLE.height && size.width >= HORIZONTAL_RECTANGLE.width -> {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AlbumArt(artUri, GlanceModifier.size(HORIZONTAL_RECTANGLE.height))
                                    Spacer(GlanceModifier.width(8.dp))
                                    PlayPauseButton(modifier = GlanceModifier.size(40.dp), state = playPauseIcon) { }
                                }
                            }

                            else -> {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    PlayPauseButton(modifier = GlanceModifier.size(40.dp), state = playPauseIcon) { }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val HORIZONTAL_RECTANGLE = DpSize(150.dp, 100.dp)
        private val SMALL_SQUARE = DpSize(150.dp, 150.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }
}

@Composable
private fun AlbumArt(
    imageUri: Uri,
    modifier: GlanceModifier = GlanceModifier
) {
    WidgetAsyncImage(uri = imageUri, modifier = modifier)
}

@Composable
fun PodcastText(title: String, subtitle: String, modifier: GlanceModifier = GlanceModifier) {
    val fgColor = GlanceTheme.colors.onPrimaryContainer
    val size = LocalSize.current
    when {
        size.height >= Sizes.short -> Column(modifier) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fgColor,
                ),
                maxLines = 2,
            )
            Text(
                text = subtitle,
                style = TextStyle(fontSize = 14.sp, color = fgColor),
                maxLines = 2,
            )
        }

        else -> Column(modifier) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = fgColor,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    modifier: GlanceModifier = GlanceModifier.size(Sizes.normal),
    state: PlayPauseIcon,
    onClick: () -> Unit
) {
    val (iconRes: Int, description: String) = when (state) {
        PlayPauseIcon.Play -> R.drawable.outline_play_arrow_24 to "Play"
        PlayPauseIcon.Pause -> R.drawable.outline_pause_24 to "Pause"
    }

    val provider = ImageProvider(iconRes)

    SquareIconButton(
        modifier = modifier,
        imageProvider = provider,
        contentDescription = description,
        onClick = onClick,
    )
}

enum class PlayPauseIcon { Play, Pause }

/**
 * Uses Coil to load images.
 */
@Composable
private fun WidgetAsyncImage(
    uri: Uri,
    modifier: GlanceModifier = GlanceModifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = uri) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(1000, 1000)
            .target { data: Drawable ->
                bitmap = (data as BitmapDrawable).bitmap
            }
            .build()

        val result = ImageLoader(context).execute(request)
        if (result is ErrorResult) {
            val t = result.throwable
            Log.e(TAG, "Image request error:", t)
        }
    }

    bitmap?.let { bitmap ->
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.cornerRadius(12.dp),
        )
    }
}

private const val TAG = "AppWidget"

data class AppWidgetViewState(
    val episodeTitle: String,
    val podcastTitle: String,
    val isPlaying: Boolean,
    val albumArtUri: String,
)

private object Sizes {
    val short = 72.dp
    val minWidth = 140.dp
    val smallBucketCutoffWidth = 250.dp // anything from minWidth to this will have no title

    val normal = 80.dp
    val medium = 56.dp
    val condensed = 48.dp
}



