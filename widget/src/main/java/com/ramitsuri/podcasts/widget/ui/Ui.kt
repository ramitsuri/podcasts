package com.ramitsuri.podcasts.widget.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import coil.ImageLoader
import coil.request.ErrorResult
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.imageRequest
import com.ramitsuri.podcasts.widget.AppWidget.Companion.BIG_RECTANGLE
import com.ramitsuri.podcasts.widget.AppWidget.Companion.BIG_SQUARE
import com.ramitsuri.podcasts.widget.AppWidget.Companion.SMALL_RECTANGLE
import com.ramitsuri.podcasts.widget.AppWidget.Companion.SMALL_SQUARE
import com.ramitsuri.podcasts.widget.R
import com.ramitsuri.podcasts.widget.action.WidgetAction
import com.ramitsuri.podcasts.widget.data.WidgetState

@Composable
internal fun AppWidgetUi(state: WidgetState) {
    val size = LocalSize.current

    GlanceTheme {
        Scaffold {
            when (state) {
                is WidgetState.CurrentlyPlaying -> {
                    CurrentlyPlayingUi(state, size)
                }

                is WidgetState.NeverPlayed -> {
                    NeverPlayedUi()
                }
            }
        }
    }
}

@Composable
private fun NeverPlayedUi() {
    val modifier =
        GlanceModifier
            .fillMaxSize()
            .let { modifier ->
                LocalContext
                    .current
                    .packageManager
                    .getLaunchIntentForPackage(LocalContext.current.packageName)
                    ?.let { intent ->
                        modifier.clickable(actionStartActivity(intent))
                    }
                    ?: modifier
            }
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Title(
            text = LocalContext.current.getString(R.string.open_app),
            size = 14.sp,
            maxLines = 3,
        )
    }
}

@Composable
private fun CurrentlyPlayingUi(
    state: WidgetState.CurrentlyPlaying,
    size: DpSize,
) {
    val artUri = state.albumArtUri
    val uri = state.deepLinkUrl.toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    Column(
        modifier =
            GlanceModifier.fillMaxSize()
                .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (size) {
            BIG_SQUARE -> {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlbumArt(imageUrl = artUri, size = 100.dp)
                    Spacer(GlanceModifier.width(8.dp))
                    Title(
                        text = state.episodeTitle,
                        size = 14.sp,
                        maxLines = 3,
                    )
                }
                Spacer(GlanceModifier.height(16.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReplayButton(size = 48.dp)
                    Spacer(GlanceModifier.width(8.dp))
                    PlayPauseButton(
                        size = 64.dp,
                        playing = state.isPlaying,
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    SkipButton(size = 48.dp)
                }
            }

            BIG_RECTANGLE -> {
                Title(
                    text = state.episodeTitle,
                    size = 14.sp,
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlbumArt(imageUrl = artUri, size = 56.dp)
                    Spacer(GlanceModifier.defaultWeight())
                    ReplayButton(size = 48.dp)
                    Spacer(GlanceModifier.width(8.dp))
                    PlayPauseButton(
                        size = 56.dp,
                        playing = state.isPlaying,
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    SkipButton(size = 48.dp)
                }
            }

            SMALL_SQUARE -> {
                Title(
                    text = state.episodeTitle,
                    size = 14.sp,
                    maxLines = 3,
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlbumArt(imageUrl = artUri, size = 64.dp)
                    Spacer(GlanceModifier.defaultWeight())
                    PlayPauseButton(size = 64.dp, state.isPlaying)
                }
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReplayButton(size = 48.dp)
                    Spacer(GlanceModifier.width(16.dp))
                    SkipButton(size = 48.dp)
                }
            }

            SMALL_RECTANGLE -> {
                Title(
                    text = state.episodeTitle,
                    size = 12.sp,
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlbumArt(imageUrl = artUri, size = 48.dp)
                    Spacer(GlanceModifier.width(8.dp))
                    PlayPauseButton(size = 48.dp, state.isPlaying)
                }
            }
        }
    }
}

@Composable
private fun AlbumArt(
    imageUrl: String,
    size: Dp,
) {
    WidgetAsyncImage(url = imageUrl, modifier = GlanceModifier.size(size))
}

@Composable
fun Title(
    text: String,
    size: TextUnit,
    maxLines: Int,
) {
    Text(
        text = text,
        style =
            TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer,
                textAlign = TextAlign.Center,
            ),
        maxLines = maxLines,
    )
}

@Composable
private fun PlayPauseButton(
    size: Dp,
    playing: Boolean,
) {
    val iconRes =
        if (playing) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
    PrimaryButton(
        modifier = GlanceModifier.size(size),
        iconRes = iconRes,
        onClick =
            if (playing) {
                WidgetAction.pause()
            } else {
                WidgetAction.play()
            },
    )
}

@Composable
private fun SkipButton(size: Dp) {
    SecondaryButton(
        modifier = GlanceModifier.size(size),
        iconRes = R.drawable.ic_skip_30,
        onClick = WidgetAction.skip(),
    )
}

@Composable
private fun ReplayButton(size: Dp) {
    SecondaryButton(
        modifier = GlanceModifier.size(size),
        iconRes = R.drawable.ic_replay_10,
        onClick = WidgetAction.replay(),
    )
}

@Composable
private fun PrimaryButton(
    modifier: GlanceModifier = GlanceModifier,
    @DrawableRes iconRes: Int,
    onClick: Action,
) {
    val provider = ImageProvider(iconRes)

    SquareIconButton(
        modifier = modifier,
        imageProvider = provider,
        contentDescription = null,
        onClick = onClick,
        backgroundColor = GlanceTheme.colors.primary,
        contentColor = GlanceTheme.colors.onPrimary,
    )
}

@Composable
private fun SecondaryButton(
    modifier: GlanceModifier = GlanceModifier,
    @DrawableRes iconRes: Int,
    onClick: Action,
) {
    val provider = ImageProvider(iconRes)

    SquareIconButton(
        modifier = modifier,
        imageProvider = provider,
        contentDescription = null,
        onClick = onClick,
        backgroundColor = GlanceTheme.colors.secondary,
        contentColor = GlanceTheme.colors.onSecondary,
    )
}

@Composable
private fun WidgetAsyncImage(
    url: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = url) {
        LogHelper.d("WidgetAsyncImage", "Fetching new image: $url")
        val request =
            context
                .imageRequest(url)
                .size(600, 600)
                .target { data: Drawable ->
                    bitmap = (data as BitmapDrawable).bitmap
                }
                .build()

        val result = ImageLoader(context).execute(request)
        if (result is ErrorResult) {
            val t = result.throwable
            LogHelper.v("WidgetAsyncImage", "Error loading image: $t")
        }
    }

    bitmap?.let { bmp ->
        Image(
            provider = ImageProvider(bmp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.cornerRadius(12.dp),
        )
    }
}
