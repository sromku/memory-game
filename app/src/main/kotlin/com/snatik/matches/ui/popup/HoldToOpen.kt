package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

/**
 * Makes [view] act only when held for [holdMillis]: a tap calls [onTap] (to say "hold me"), a hold
 * calls [onHeld] once, and [onProgress] reports 0..1 while the finger is down. A gate a child will
 * not pass by accident, without any reading.
 */
@SuppressLint("ClickableViewAccessibility") // performClick is called on a tap; a hold is the point
class HoldToOpen(
    private val view: View,
    private val holdMillis: Long,
    private val onProgress: (Float) -> Unit,
    private val onTap: () -> Unit,
    private val onHeld: () -> Unit,
) {
    private var downAt = 0L
    private var fired = false
    private val tick = object : Runnable {
        override fun run() {
            val progress = ((System.currentTimeMillis() - downAt).toFloat() / holdMillis).coerceIn(0f, 1f)
            onProgress(progress)
            if (progress >= 1f) {
                fired = true
                onHeld()
            } else {
                view.postDelayed(this, TICK_MS)
            }
        }
    }

    init {
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downAt = System.currentTimeMillis()
                    fired = false
                    view.postDelayed(tick, TICK_MS)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(tick)
                    onProgress(0f)
                    if (!fired && event.actionMasked == MotionEvent.ACTION_UP) {
                        view.performClick()
                        onTap()
                    }
                }
            }
            true
        }
        // Screen readers reach the same place through a long click.
        view.setOnLongClickListener { onHeld(); true }
    }

    private companion object {
        const val TICK_MS = 40L
    }
}
