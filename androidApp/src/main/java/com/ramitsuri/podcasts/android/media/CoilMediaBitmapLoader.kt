package com.ramitsuri.podcasts.android.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future

@UnstableApi
class CoilMediaBitmapLoader(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : BitmapLoader {
    private val imageLoader = ImageLoader(context)

    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return coroutineScope.future {
            val request =
                ImageRequest.Builder(context)
                    .data(uri)
                    // Notifications require software bitmaps
                    .allowHardware(false)
                    .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as BitmapDrawable).bitmap
            } else {
                throw (result as? coil.request.ErrorResult)?.throwable
                    ?: Exception("Failed to load bitmap")
            }
        }
    }
}
