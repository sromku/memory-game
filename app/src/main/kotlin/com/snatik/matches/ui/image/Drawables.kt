package com.snatik.matches.ui.image

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

/**
 * A UI drawable, ready to show. The traced UI vectors carry hundreds to thousands of paths, so
 * they come from [ArtCache], rasterised once off the main thread and shared.
 */
suspend fun Context.loadDrawable(@DrawableRes id: Int): Drawable = ArtCache.drawable(this, id)

/** Renders drawables a coming screen will show, so it finds them ready. */
suspend fun Context.warmDrawables(@DrawableRes vararg ids: Int) = ArtCache.warm(this, ids.asIterable())
