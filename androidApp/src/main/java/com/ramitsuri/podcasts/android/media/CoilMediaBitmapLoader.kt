package com.ramitsuri.podcasts.android.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil.imageLoader
import coil.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import com.ramitsuri.podcasts.utils.imageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future

@UnstableApi
class CoilMediaBitmapLoader(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return coroutineScope.future {
            val request =
                context
                    .imageRequest(uri.toString())
                    .allowHardware(false)
                    .build()

            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
                    ?: throw Exception("Resulting drawable is not a BitmapDrawable")
            } else {
                throw (result as? coil.request.ErrorResult)?.throwable
                    ?: Exception("Failed to load bitmap")
            }
        }
    }
}
