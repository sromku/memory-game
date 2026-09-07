package com.snatik.matches.ui.image

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Size
import androidx.annotation.DrawableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The maths of ImageView's centerCrop, so views drawn over a background can line up with its art. */
object CenterCrop {

    /**
     * Where a horizontal line at [imageFraction] of an image's height lands, in pixels from the
     * top of a view of [viewWidth] x [viewHeight] showing the image centre-cropped.
     */
    fun y(imageFraction: Float, imageWidth: Int, imageHeight: Int, viewWidth: Int, viewHeight: Int): Float {
        val scale = maxOf(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
        val offset = (imageHeight * scale - viewHeight) / 2f
        return imageFraction * imageHeight * scale - offset
    }

    /** The pixel size of a drawable resource, read from its header without decoding it. */
    suspend fun imageSize(resources: Resources, @DrawableRes id: Int): Size = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, id, options)
        Size(options.outWidth, options.outHeight)
    }
}
