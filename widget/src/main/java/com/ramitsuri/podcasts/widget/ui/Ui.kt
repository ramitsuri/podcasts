package com.ramitsuri.podcasts.widget.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
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
import com.ramitsuri.podcasts.widget.R
import com.ramitsuri.podcasts.widget.action.WidgetAction
import com.ramitsuri.podcasts.widget.data.WidgetState

@Composable
internal fun AppWidgetUi(
    state: WidgetState,
    albumArtBitmap: Bitmap?,
) {
    val size = LocalSize.current

    GlanceTheme {
        Scaffold(horizontalPadding = 0.dp) {
            when (state) {
                is WidgetState.CurrentlyPlaying -> {
                    CurrentlyPlayingUi(state, size, albumArtBitmap)
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
    albumArtBitmap: Bitmap?,
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
            WidgetLayout.Arrangement.ArtBesideTitle -> ArtBesideTitleUi(state, layout, albumArtBitmap)
            WidgetLayout.Arrangement.SingleRow -> SingleRowUi(state, layout, albumArtBitmap)
            WidgetLayout.Arrangement.SingleColumn -> SingleColumnUi(state, layout, albumArtBitmap)
            WidgetLayout.Arrangement.TwoRows -> TwoRowsUi(state, layout, albumArtBitmap)
        }
    }
}

@Composable
private fun ArtBesideTitleUi(
    state: WidgetState.CurrentlyPlaying,
    layout: WidgetLayout,
    albumArtBitmap: Bitmap?,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        layout.artSize?.let { artSize ->
            AlbumArt(bitmap = albumArtBitmap, size = artSize)
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
    albumArtBitmap: Bitmap?,
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
            AlbumArt(bitmap = albumArtBitmap, size = artSize)
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
    albumArtBitmap: Bitmap?,
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
            AlbumArt(bitmap = albumArtBitmap, size = artSize)
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
    albumArtBitmap: Bitmap?,
) {
    layout.artSize?.let { artSize ->
        AlbumArt(bitmap = albumArtBitmap, size = artSize)
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
    bitmap: Bitmap?,
    size: Dp,
) {
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.size(size).cornerRadius(12.dp),
        )
    } else {
        Spacer(GlanceModifier.size(size))
    }
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
