package com.snatik.matches.ui.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decodes drawables off the main thread, subsampled so they are no larger than needed. */
object BitmapLoader {

    suspend fun decodeSampled(
        resources: Resources,
        @DrawableRes id: Int,
        requiredWidth: Int,
        requiredHeight: Int,
    ): Bitmap = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, id, options)
        options.inSampleSize = sampleSize(options.outWidth, options.outHeight, requiredWidth, requiredHeight)
        options.inJustDecodeBounds = false
        BitmapFactory.decodeResource(resources, id, options) ?: error("Could not decode drawable $id")
    }

    /**
     * Largest power-of-two factor that keeps both decoded dimensions at or above the required size.
     */
    internal fun sampleSize(width: Int, height: Int, requiredWidth: Int, requiredHeight: Int): Int {
        if (requiredWidth <= 0 || requiredHeight <= 0) return 1
        var sample = 1
        while (width / (sample * 2) >= requiredWidth && height / (sample * 2) >= requiredHeight) {
            sample *= 2
        }
        return sample
    }
}
