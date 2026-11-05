package com.ramitsuri.podcasts.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.scale
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.SuccessResult
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.navigation.Route
import com.ramitsuri.podcasts.navigation.deepLinkWithArgValue
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.imageRequest
import com.ramitsuri.podcasts.widget.data.WidgetDefinition
import com.ramitsuri.podcasts.widget.data.WidgetState
import com.ramitsuri.podcasts.widget.receiver.WidgetReceiver
import com.ramitsuri.podcasts.widget.ui.AppWidgetUi
import com.ramitsuri.podcasts.widget.ui.WidgetLayout
import java.io.File
import java.io.FileOutputStream

class AppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(WidgetLayout.RESPONSIVE_SIZES)

    override val stateDefinition = WidgetDefinition

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable,
    ) {
        LogHelper.d(TAG, "Composition error: $throwable")
    }

    override suspend fun onDelete(
        context: Context,
        glanceId: GlanceId,
    ) {
        super.onDelete(context, glanceId)
        LogHelper.d(TAG, "Deleted widget $glanceId")
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val state = getAppWidgetState<WidgetState>(context, id)
        val initialUri = (state as? WidgetState.CurrentlyPlaying)?.albumArtUri.orEmpty()
        var cachedUri: String? = initialUri
        var cachedBitmap: Bitmap? =
            if (initialUri.isNotEmpty()) {
                loadAlbumArt(context, initialUri)
            } else {
                null
            }

        provideContent {
            val currentState = currentState<WidgetState>()
            val uri = (currentState as? WidgetState.CurrentlyPlaying)?.albumArtUri.orEmpty()
            if (uri != cachedUri || (cachedBitmap == null && uri.isNotEmpty())) {
                cachedUri = uri
                cachedBitmap =
                    if (uri.isNotEmpty()) {
                        loadAlbumArtSynchronously(context, uri)
                    } else {
                        null
                    }
            }
            AppWidgetUi(currentState, cachedBitmap)
        }
    }

    private fun loadAlbumArtSynchronously(
        context: Context,
        url: String,
    ): Bitmap? {
        if (url.isEmpty()) return null
        val cachedWidget = context.imageLoader.memoryCache?.get(MemoryCache.Key(cacheKey(url)))?.bitmap
        if (cachedWidget != null) {
            return prepareWidgetBitmap(cachedWidget)
        }
        val cachedFull = context.imageLoader.memoryCache?.get(MemoryCache.Key(url))?.bitmap
        if (cachedFull != null) {
            val scaled = prepareWidgetBitmap(cachedFull)
            context.imageLoader.memoryCache?.set(
                MemoryCache.Key(cacheKey(url)),
                MemoryCache.Value(bitmap = scaled),
            )
            saveBitmapToFile(context, scaled)
            return scaled
        }
        val artFile = File(context.filesDir, ALBUM_ART_FILE)
        return decodeFileSafely(artFile)
    }

    private suspend fun loadAlbumArt(
        context: Context,
        url: String,
    ): Bitmap? {
        if (url.isEmpty()) return null
        val cachedWidget = context.imageLoader.memoryCache?.get(MemoryCache.Key(cacheKey(url)))?.bitmap
        if (cachedWidget != null) {
            val prepared = prepareWidgetBitmap(cachedWidget)
            saveBitmapToFile(context, prepared)
            return prepared
        }
        val cachedFull = context.imageLoader.memoryCache?.get(MemoryCache.Key(url))?.bitmap
        if (cachedFull != null) {
            val scaled = prepareWidgetBitmap(cachedFull)
            context.imageLoader.memoryCache?.set(
                MemoryCache.Key(cacheKey(url)),
                MemoryCache.Value(bitmap = scaled),
            )
            saveBitmapToFile(context, scaled)
            return scaled
        }
        val request =
            context
                .imageRequest(url)
                .memoryCacheKey(cacheKey(url))
                .size(MAX_BITMAP_SIZE, MAX_BITMAP_SIZE)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .allowHardware(enable = false)
                .build()
        val result = context.imageLoader.execute(request)
        val bitmap =
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap?.let { prepareWidgetBitmap(it) }
            } else {
                null
            }
        if (bitmap != null) {
            saveBitmapToFile(context, bitmap)
            return bitmap
        }
        val artFile = File(context.filesDir, ALBUM_ART_FILE)
        return decodeFileSafely(artFile)
    }

    companion object {
        private const val TAG = "AppWidget"
        private const val ALBUM_ART_FILE = "widget_album_art.png"
        private const val MAX_BITMAP_SIZE = 256
        private const val WIDGET_CACHE_PREFIX = "widget_"

        private fun cacheKey(url: String) = "$WIDGET_CACHE_PREFIX$url"

        private fun prepareWidgetBitmap(bitmap: Bitmap): Bitmap {
            val maxDimension = maxOf(bitmap.width, bitmap.height)
            val scaled =
                if (maxDimension > MAX_BITMAP_SIZE) {
                    val scale = MAX_BITMAP_SIZE.toFloat() / maxDimension
                    val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
                    val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
                    bitmap.scale(targetWidth, targetHeight)
                } else {
                    bitmap
                }
            return if (scaled.config != Bitmap.Config.RGB_565) {
                scaled.copy(Bitmap.Config.RGB_565, false) ?: scaled
            } else {
                scaled
            }
        }

        private fun decodeFileSafely(file: File): Bitmap? {
            if (!file.exists()) return null
            return runCatching {
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
                val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
                var sampleSize = 1
                while (maxDim / (sampleSize * 2) >= MAX_BITMAP_SIZE) {
                    sampleSize *= 2
                }
                val decodeOptions =
                    BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null
                prepareWidgetBitmap(decoded)
            }.getOrNull()
        }

        private fun saveBitmapToFile(
            context: Context,
            bitmap: Bitmap,
        ) {
            runCatching {
                val prepared = prepareWidgetBitmap(bitmap)
                val file = File(context.filesDir, ALBUM_ART_FILE)
                FileOutputStream(file).use { out ->
                    prepared.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }

        suspend fun Context.addWidget() {
            GlanceAppWidgetManager(applicationContext)
                .requestPinGlanceAppWidget(receiver = WidgetReceiver::class.java)
        }

        suspend fun Context.updateWidget(
            episode: Episode,
            playing: Boolean,
        ) {
            val appWidgetManager = GlanceAppWidgetManager(this)
            val url =
                Route.EpisodeDetails(
                    episodeId = episode.id,
                    podcastId = episode.podcastId,
                ).deepLinkWithArgValue ?: return

            // Pre-load and cache the album art before updating widget state
            AppWidget().loadAlbumArt(this, episode.podcastImageUrl)

            appWidgetManager.getGlanceIds(AppWidget::class.java).forEach { glanceId ->
                updateAppWidgetState(
                    context = this,
                    definition = WidgetDefinition,
                    glanceId = glanceId,
                    updateState = {
                        WidgetState.CurrentlyPlaying(
                            episodeTitle = episode.title,
                            isPlaying = playing,
                            albumArtUri = episode.podcastImageUrl,
                            deepLinkUrl = url,
                        )
                    },
                )
                AppWidget().update(this, glanceId)
            }
        }
    }
}
