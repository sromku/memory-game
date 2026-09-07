package com.snatik.matches.ui.minigame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.snatik.matches.R

/** A row of dots: one per note of the song, gold for the notes already learned. */
class SongProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var total = 0
    private var learned = 0

    private val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.road_gold) }
    private val slot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.road_star_slot) }
    private val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.road_rim)
    }

    fun set(total: Int, learned: Int) {
        this.total = total
        this.learned = learned.coerceIn(0, total)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (total == 0) return
        val radius = height * 0.36f
        val gap = radius * 0.9f
        val span = total * 2 * radius + (total - 1) * gap
        var cx = (width - span) / 2f + radius
        val cy = height / 2f
        rim.strokeWidth = radius * 0.18f
        for (i in 0 until total) {
            canvas.drawCircle(cx, cy, radius, if (i < learned) gold else slot)
            canvas.drawCircle(cx, cy, radius, rim)
            cx += 2 * radius + gap
        }
    }
}
