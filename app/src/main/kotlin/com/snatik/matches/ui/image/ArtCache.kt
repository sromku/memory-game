package com.snatik.matches.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The traced UI vectors, rasterised once and kept. A button of 1,600 paths costs a few hundred
 * milliseconds to parse and paint, so without this every screen popped in piece by piece.
 * Bitmaps are rendered at the vector's own size off the main thread, shared between views, and
 * warmed ahead of the screens that need them (see [ArtWarmup]).
 */
object ArtCache {

    private val bitmaps = object : LruCache<Int, Bitmap>(CACHE_BYTES) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
    }
    private val inFlight = mutableMapOf<Int, Deferred<Bitmap>>()
    private val lock = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** A drawable of the rendered vector; each caller gets its own instance over the shared bitmap. */
    suspend fun drawable(context: Context, @DrawableRes id: Int): Drawable =
        bitmap(context, id).toDrawable(context.resources)

    suspend fun bitmap(context: Context, @DrawableRes id: Int): Bitmap {
        bitmaps.get(id)?.let { return it }
        val app = context.applicationContext
        val rendering = lock.withLock {
            inFlight.getOrPut(id) {
                scope.async {
                    render(app, id).also { bitmap ->
                        bitmaps.put(id, bitmap)
                        lock.withLock { inFlight.remove(id) }
                    }
                }
            }
        }
        return rendering.await()
    }

    /** Renders what is not there yet, one at a time so the screen keeps its cores. */
    suspend fun warm(context: Context, ids: Iterable<Int>) {
        for (id in ids) bitmap(context, id)
    }

    private fun render(context: Context, @DrawableRes id: Int): Bitmap {
        val drawable = ResourcesCompat.getDrawable(context.resources, id, context.theme) ?: error("Missing drawable $id")
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(w, h))
        val bitmap = drawable.toBitmap((w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1))
        // Graphics memory instead of the Java heap where the platform allows it.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) bitmap.copy(Bitmap.Config.HARDWARE, false) ?: bitmap else bitmap
    }

    private const val CACHE_BYTES = 96 * 1024 * 1024
    private const val MAX_SIDE = 800
}
