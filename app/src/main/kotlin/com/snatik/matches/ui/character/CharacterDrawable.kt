package com.snatik.matches.ui.character

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.PI
import kotlin.math.sin

/**
 * Draws a [Character] from its vector paths, so it is crisp at any size, and brings it to life:
 * an idle loop (breathing squash-and-stretch, a slow sway, blinking) and a one-off happy hop.
 */
class CharacterDrawable(private val character: Character) : Drawable(), Animatable {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    private val partMatrix = Matrix()
    private val scratch = android.graphics.Path()

    /** 0..1 progress of the idle loop, driven by [idle]. */
    private var phase = 0f
    private var blink = 0f      // 1 = eyes fully closed
    private var hop = 0f        // 0 = on the ground, 1 = top of the hop
    private var hopRotation = 0f

    private val idle = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = IDLE_PERIOD_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = null
        addUpdateListener { phase = it.animatedValue as Float; invalidateSelf() }
    }
    private val blinker = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
        duration = BLINK_MS
        startDelay = 1400
        addUpdateListener { blink = it.animatedValue as Float; invalidateSelf() }
        doOnEndRestart()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val scale = minOf(b.width() / character.width, b.height() / character.height)
        val drawnW = character.width * scale
        val drawnH = character.height * scale
        val left = b.left + (b.width() - drawnW) / 2
        val top = b.top + (b.height() - drawnH) / 2

        // Breathing: 3% taller and 2% narrower at the peak, anchored at the feet. Sway: 2 degrees.
        val breath = sin(phase * 2 * PI).toFloat()
        val squash = if (hop > 0f) 1f + 0.12f * hop else 1f + 0.03f * breath
        val stretchX = if (hop > 0f) 1f - 0.08f * hop else 1f - 0.02f * breath
        val sway = if (hop > 0f) hopRotation else 2f * sin(phase * 2 * PI + 1).toFloat()
        val lift = hop * drawnH * 0.18f

        matrix.reset()
        matrix.postTranslate(-character.width / 2, -character.height)          // pivot at the feet
        matrix.postScale(stretchX, squash)
        matrix.postRotate(sway)
        matrix.postTranslate(character.width / 2, character.height)
        matrix.postScale(scale, scale)
        matrix.postTranslate(left, top - lift)

        val eyes = character.hasEyes
        for (part in character.parts) {
            fill.color = part.color
            // Paths are transformed in software into one reused scratch path: drawing the original
            // path under a canvas transform is not reliably rendered by the hardware canvas.
            when (part.group) {
                Character.Group.SHADOW -> {
                    // The shadow stays on the ground and shrinks while the character is in the air.
                    partMatrix.set(matrix)
                    partMatrix.preTranslate(0f, lift / scale)
                    val s = 1f - 0.4f * hop
                    partMatrix.preScale(s, s, part.bounds.centerX(), part.bounds.centerY())
                    fill.alpha = (255 * (1f - 0.5f * hop)).toInt()
                    drawTransformed(canvas, part, partMatrix)
                    fill.alpha = 255
                }
                Character.Group.EYE, Character.Group.PUPIL -> {
                    partMatrix.set(matrix)
                    // Each eye closes around its own centre, so many-eyed monsters blink too.
                    if (eyes && blink > 0f) partMatrix.preScale(1f, 1f - 0.92f * blink, part.bounds.centerX(), part.bounds.centerY())
                    drawTransformed(canvas, part, partMatrix)
                }
                Character.Group.BODY -> drawTransformed(canvas, part, matrix)
            }
        }
    }

    private fun drawTransformed(canvas: Canvas, part: Character.Part, m: Matrix) {
        part.path.transform(m, scratch)
        canvas.drawPath(scratch, fill)
    }

    /** A quick joyful jump, used when the card's pair is found. */
    fun hop() {
        ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = HOP_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                hop = it.animatedValue as Float
                hopRotation = 12f * sin(it.animatedFraction * PI).toFloat()
                invalidateSelf()
            }
            start()
        }
    }

    override fun start() {
        if (!idle.isRunning) idle.start()
        if (!blinker.isRunning) blinker.start()
    }

    override fun stop() {
        idle.cancel()
        blinker.cancel()
        phase = 0f; blink = 0f; hop = 0f
        invalidateSelf()
    }

    override fun isRunning(): Boolean = idle.isRunning

    override fun setAlpha(alpha: Int) { fill.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { fill.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java") override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private fun ValueAnimator.doOnEndRestart() {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (idle.isRunning) { startDelay = (1800..4200).random().toLong(); start() }
            }
        })
    }

    private companion object {
        const val IDLE_PERIOD_MS = 2600L
        const val BLINK_MS = 180L
        const val HOP_MS = 520L
    }
}
