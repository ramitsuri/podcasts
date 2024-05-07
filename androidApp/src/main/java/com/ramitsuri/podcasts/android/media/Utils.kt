package com.ramitsuri.podcasts.android.media

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.ramitsuri.podcasts.model.Episode

fun Episode.asMediaItem(artworkUriOverride: Uri? = null): MediaItem {
    val metadata =
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(podcastName)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setArtworkUri(artworkUriOverride ?: podcastImageUrl.toUri())
            .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
            .build()
    val uri =
        Uri.Builder()
            .encodedPath(enclosureUrl)
            .build()
    return MediaItem.fromUri(uri)
        .buildUpon()
        .setMediaId(id)
        .setMediaMetadata(metadata)
        .build()
}

context(Context)
@OptIn(ExperimentalCoilApi::class)
val Episode.cachedArtworkUri: Uri?
    get() =
        imageLoader.diskCache?.openSnapshot(podcastImageUrl)?.use { snapshot ->
            snapshot.data.toFile().toUri()
        }
