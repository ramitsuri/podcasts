package com.ramitsuri.podcasts.widget.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import coil.imageLoader
import coil.request.ErrorResult
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.imageRequest
import com.ramitsuri.podcasts.widget.R
import com.ramitsuri.podcasts.widget.action.WidgetAction
import com.ramitsuri.podcasts.widget.data.WidgetState

@Composable
internal fun AppWidgetUi(state: WidgetState) {
    val size = LocalSize.current

    GlanceTheme {
        Scaffold(horizontalPadding = 0.dp) {
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
    val uri = state.deepLinkUrl.toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val fontScale = LocalContext.current.resources.configuration.fontScale
    val layout = WidgetLayout.compute(width = size.width, height = size.height, fontScale = fontScale)
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .padding(
                    horizontal = layout.horizontalPadding,
                    vertical = WidgetLayout.VERTICAL_PADDING,
                )
                .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (layout.arrangement) {
            WidgetLayout.Arrangement.ArtBesideTitle -> ArtBesideTitleUi(state, layout)
            WidgetLayout.Arrangement.SingleRow -> SingleRowUi(state, layout)
            WidgetLayout.Arrangement.SingleColumn -> SingleColumnUi(state, layout)
            WidgetLayout.Arrangement.TwoRows -> TwoRowsUi(state, layout)
        }
    }
}

@Composable
private fun ArtBesideTitleUi(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        layout.artSize?.let { artSize ->
            AlbumArt(imageUrl = state.albumArtUri, size = artSize)
            Spacer(GlanceModifier.width(WidgetLayout.GAP))
        }
        Title(
            text = state.episodeTitle,
            size = 14.sp,
            maxLines = layout.titleLines,
            textAlign = TextAlign.Start,
            modifier = GlanceModifier.defaultWeight(),
        )
    }
    Spacer(GlanceModifier.height(WidgetLayout.GAP))
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Controls(state, layout)
    }
}

@Composable
private fun SingleRowUi(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
) {
    if (layout.titleLines > 0) {
        Title(
            text = state.episodeTitle,
            size = 14.sp,
            maxLines = layout.titleLines,
        )
        Spacer(GlanceModifier.height(WidgetLayout.TITLE_GAP))
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        layout.artSize?.let { artSize ->
            AlbumArt(imageUrl = state.albumArtUri, size = artSize)
            if (layout.secondarySize != null) {
                // Push controls to the end when the full row is shown
                Spacer(GlanceModifier.defaultWeight())
            } else {
                Spacer(GlanceModifier.width(WidgetLayout.GAP))
            }
        }
        Controls(state, layout)
    }
}

@Composable
private fun TwoRowsUi(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
) {
    if (layout.titleLines > 0) {
        Title(
            text = state.episodeTitle,
            size = 14.sp,
            maxLines = layout.titleLines,
        )
        Spacer(GlanceModifier.height(WidgetLayout.TITLE_GAP))
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        layout.artSize?.let { artSize ->
            AlbumArt(imageUrl = state.albumArtUri, size = artSize)
            Spacer(GlanceModifier.width(WidgetLayout.GAP))
        }
        PlayPauseButton(size = layout.playSize, playing = state.isPlaying)
    }
    layout.secondarySize?.let { secondarySize ->
        Spacer(GlanceModifier.height(WidgetLayout.GAP))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReplayButton(size = secondarySize)
            Spacer(GlanceModifier.width(WidgetLayout.GAP))
            SkipButton(size = secondarySize)
        }
    }
}

@Composable
private fun SingleColumnUi(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
) {
    layout.artSize?.let { artSize ->
        AlbumArt(imageUrl = state.albumArtUri, size = artSize)
        Spacer(GlanceModifier.height(WidgetLayout.GAP))
    }
    if (layout.titleLines > 0) {
        Title(
            text = state.episodeTitle,
            size = 14.sp,
            maxLines = layout.titleLines,
        )
        Spacer(GlanceModifier.height(WidgetLayout.TITLE_GAP))
    }
    val secondarySize = layout.secondarySize
    if (secondarySize != null) {
        ReplayButton(size = secondarySize)
        Spacer(GlanceModifier.height(WidgetLayout.GAP))
    }
    PlayPauseButton(size = layout.playSize, playing = state.isPlaying)
    if (secondarySize != null) {
        Spacer(GlanceModifier.height(WidgetLayout.GAP))
        SkipButton(size = secondarySize)
    }
}

@Composable
private fun Controls(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
) {
    val secondarySize = layout.secondarySize
    if (secondarySize != null) {
        ReplayButton(size = secondarySize)
        Spacer(GlanceModifier.width(WidgetLayout.GAP))
    }
    PlayPauseButton(size = layout.playSize, playing = state.isPlaying)
    if (secondarySize != null) {
        Spacer(GlanceModifier.width(WidgetLayout.GAP))
        SkipButton(size = secondarySize)
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
    textAlign: TextAlign = TextAlign.Center,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style =
            TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer,
                textAlign = textAlign,
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
                .allowHardware(false)
                .build()

        val result = context.imageLoader.execute(request)
        if (result is coil.request.SuccessResult) {
            LogHelper.v("WidgetAsyncImage", "Loaded image")
            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                bitmap = drawable.bitmap
            }
        } else if (result is ErrorResult) {
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
