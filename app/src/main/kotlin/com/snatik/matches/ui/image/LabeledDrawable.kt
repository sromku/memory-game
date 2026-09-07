package com.snatik.matches.ui.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.snatik.matches.R

/**
 * A word written on a picture, placed by fractions of the picture: the pictures carry no
 * lettering of their own so every language can have its own. Text is white with the art's dark
 * shadow, sized to [height] of the picture and shrunk if it would be wider than [maxWidth].
 */
class Label(
    val text: String,
    /** Horizontal anchor: the centre of the text, or its right edge when [endAligned]. */
    val x: Float,
    val y: Float,
    val height: Float,
    val maxWidth: Float,
    val uppercase: Boolean = false,
    /** Grows to the left from [x], for words that sit beside a picture on their right. */
    val endAligned: Boolean = false,
)

/** [art] with [labels] written on it, in the game's display font of the current language. */
class LabeledDrawable(context: Context, private val art: Drawable, private val labels: List<Label>) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        typeface = ResourcesCompat.getFont(context, R.font.game)
        isFakeBoldText = context.resources.getBoolean(R.bool.game_text_fake_bold)
        textAlign = Paint.Align.CENTER
    }
    private val shadowColor = ContextCompat.getColor(context, R.color.label_shadow)
    private val locale = context.resources.configuration.locales[0]
    private val texts = labels.map { if (it.uppercase) it.text.uppercase(locale) else it.text }

    override fun draw(canvas: Canvas) {
        art.draw(canvas)
        val b = bounds
        labels.forEachIndexed { index, label ->
            val text = texts[index]
            var size = b.height() * label.height
            paint.textSize = size
            val limit = b.width() * label.maxWidth
            val width = paint.measureText(text)
            if (width > limit) {
                size *= limit / width
                paint.textSize = size
            }
            paint.setShadowLayer(size * 0.06f, size * 0.07f, size * 0.09f, shadowColor)
            paint.textAlign = if (label.endAligned) Paint.Align.RIGHT else Paint.Align.CENTER
            val cx = b.left + b.width() * label.x
            val cy = b.top + b.height() * label.y - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(text, cx, cy, paint)
        }
    }

    override fun onBoundsChange(bounds: Rect) = art.setBounds(bounds)
    override fun getIntrinsicWidth(): Int = art.intrinsicWidth
    override fun getIntrinsicHeight(): Int = art.intrinsicHeight
    override fun setAlpha(alpha: Int) { art.alpha = alpha; paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { art.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java") override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
