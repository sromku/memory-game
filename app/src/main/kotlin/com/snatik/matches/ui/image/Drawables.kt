package com.snatik.matches.ui.image

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Inflates a drawable off the main thread. The traced UI vectors carry hundreds to thousands of
 * paths; parsing them is the expensive part and Resources is thread-safe, so the parse happens on
 * a worker and only the ready drawable is handed to the view.
 */
suspend fun Context.loadDrawable(@DrawableRes id: Int): Drawable = withContext(Dispatchers.Default) {
    ResourcesCompat.getDrawable(resources, id, theme) ?: error("Missing drawable $id")
}

/**
 * Parses drawables that a layout is about to inflate, so the inflation finds them in the resource
 * cache instead of parsing on the main thread.
 */
suspend fun Context.warmDrawables(@DrawableRes vararg ids: Int) = withContext(Dispatchers.Default) {
    ids.forEach { ResourcesCompat.getDrawable(resources, it, theme) }
}
