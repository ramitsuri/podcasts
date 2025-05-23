package com.ramitsuri.podcasts.android.ui.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.imageRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun getColorFromImage(
    context: Context,
    url: String,
    isDarkTheme: Boolean,
): Color? {
    val request = context.imageRequest(url).build()
    return when (val result = context.imageLoader.execute(request)) {
        is ErrorResult -> {
            LogHelper.v("getColorFromImage", "Error loading image: ${result.throwable}")
            null
        }

        is SuccessResult -> {
            result.drawable.toBitmap().getPalette(isDarkTheme)
        }
    }
}

private suspend fun Bitmap.getPalette(isDarkTheme: Boolean): Color? {
    return suspendCancellableCoroutine { continuation ->
        Palette
            .Builder(copy(Bitmap.Config.RGBA_F16, true))
            .clearFilters()
            .maximumColorCount(8)
            .generate { palette ->
                if (palette == null) {
                    LogHelper.d("getColorFromImage", "Palette not generated")
                }
                palette?.let {
                    if (isDarkTheme) {
                        it.lightVibrantSwatch
                    } else {
                        it.darkVibrantSwatch
                    }
                }?.let {
                    continuation.resume(Color(it.rgb))
                } ?: continuation.resume(null)
            }
    }
}
