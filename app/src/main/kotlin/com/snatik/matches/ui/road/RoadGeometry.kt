package com.snatik.matches.ui.road

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Where each round of a road sits, in pixels: a gentle wave running from left to right. Pure
 * geometry, so [RoadMapView] only paints and this can be unit tested.
 */
class RoadGeometry(
    val roundCount: Int,
    /** Horizontal distance between neighbouring rounds. */
    val spacing: Float,
    /** Space before the first and after the last round. */
    val margin: Float,
    /** The wave's middle line and half of its height. */
    val centerY: Float,
    val amplitude: Float,
) {
    val contentWidth: Float = 2 * margin + (roundCount - 1) * spacing

    fun x(index: Int): Float = margin + (index - 1) * spacing

    fun y(index: Int): Float = centerY + amplitude * sin((index - 1) * WAVE_STEP).toFloat()

    /** The round whose disc of [radius] contains the point, or null. */
    fun roundAt(px: Float, py: Float, radius: Float): Int? =
        (1..roundCount).firstOrNull { hypot(px - x(it), py - y(it)) <= radius }

    /** Every round whose art, reaching [reach] around its centre, can touch the window [left, right]. */
    fun roundsWithin(left: Float, right: Float, reach: Float): IntRange {
        val first = (ceil((left - reach - margin) / spacing).toInt() + 1).coerceIn(1, roundCount)
        val last = (((right + reach - margin) / spacing).toInt() + 1).coerceIn(1, roundCount)
        return first..last
    }

    fun maxScroll(viewWidth: Int): Int = (contentWidth - viewWidth).toInt().coerceAtLeast(0)

    /** The scroll offset that centres round [index] in a window [viewWidth] wide, kept within the content. */
    fun scrollToCenter(index: Int, viewWidth: Int): Int =
        (x(index) - viewWidth / 2f).toInt().coerceIn(0, maxScroll(viewWidth))

    companion object {
        /** Rounds in one full wave of the road. */
        const val ROUNDS_PER_WAVE = 8
        private const val WAVE_STEP = 2 * PI / ROUNDS_PER_WAVE
    }
}
