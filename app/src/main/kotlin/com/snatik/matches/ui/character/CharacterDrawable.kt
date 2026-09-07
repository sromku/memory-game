package com.snatik.matches.ui.character

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Draws a [RenderedCharacter] and brings it to life: an idle loop (breathing squash-and-stretch,
 * a slow sway, blinking) and a one-off happy hop. Per frame this is two bitmap draws and a handful
 * of eye paths, cheap enough to run on every face-up card.
 */
class CharacterDrawable(private val rendered: RenderedCharacter) : Drawable(), Animatable {

    private val character = rendered.character
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()        // character units -> canvas, with the current pose
    private val partMatrix = Matrix()
    private val scratch = Path()

    private var phase = 0f              // 0..1 through the idle loop
    private var blink = 0f              // 1 = eyes fully closed
    private var hop = 0f                // 0 = on the ground, 1 = top of the hop
    private var hopRotation = 0f
    private var hopHeight = 0.18f

    /** Where the feet are, as a fraction of the drawable's height from the top. */
    val feetFraction: Float get() = character.feetY / maxOf(character.width, character.height)

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
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (idle.isRunning) { startDelay = (1800..4200).random().toLong(); start() }
            }
        })
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
        val lift = hop * drawnH * hopHeight

        matrix.reset()
        matrix.postTranslate(-character.width / 2, -character.height)          // pivot at the feet
        matrix.postScale(stretchX, squash)
        matrix.postRotate(sway)
        matrix.postTranslate(character.width / 2, character.height)
        matrix.postScale(scale, scale)
        matrix.postTranslate(left, top - lift)

        // Shadow: stays on the ground, shrinks and fades while the character is in the air.
        rendered.shadow?.let { shadow ->
            partMatrix.set(matrix)
            partMatrix.preTranslate(0f, lift / scale)
            val s = 1f - 0.4f * hop
            partMatrix.preScale(s, s, rendered.shadowOrigin.centerX(), rendered.shadowOrigin.centerY())
            partMatrix.preTranslate(rendered.shadowOrigin.left, rendered.shadowOrigin.top)
            partMatrix.preScale(1f / rendered.scale, 1f / rendered.scale)
            bitmapPaint.alpha = (255 * (1f - 0.5f * hop)).toInt()
            canvas.drawBitmap(shadow, partMatrix, bitmapPaint)
            bitmapPaint.alpha = 255
        }

        partMatrix.set(matrix)
        partMatrix.preScale(1f / rendered.scale, 1f / rendered.scale)
        canvas.drawBitmap(rendered.body, partMatrix, bitmapPaint)

        // Eyes close around their own centres, so many-eyed monsters blink too.
        for (eye in rendered.eyes) {
            eyePaint.color = eye.color
            partMatrix.set(matrix)
            if (blink > 0f) partMatrix.preScale(1f, 1f - 0.92f * blink, eye.bounds.centerX(), eye.bounds.centerY())
            eye.path.transform(partMatrix, scratch)
            canvas.drawPath(scratch, eyePaint)
        }
    }

    /** A quick joyful jump, used when the card's pair is found. Each hop leans a random way and height. */
    fun hop() {
        val lean = (if (Random.nextBoolean()) 1f else -1f) * (6f + Random.nextFloat() * 8f)
        hopHeight = 0.12f + Random.nextFloat() * 0.1f
        ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = HOP_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                hop = it.animatedValue as Float
                hopRotation = lean * sin(it.animatedFraction * PI).toFloat()
                invalidateSelf()
            }
            start()
        }
    }

    override fun start() {
        if (!idle.isRunning) idle.start()
        if (rendered.eyes.isNotEmpty() && !blinker.isRunning) blinker.start()
    }

    override fun stop() {
        idle.cancel()
        blinker.cancel()
        phase = 0f; blink = 0f; hop = 0f
        invalidateSelf()
    }

    override fun isRunning(): Boolean = idle.isRunning

    override fun setAlpha(alpha: Int) { bitmapPaint.alpha = alpha; eyePaint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { bitmapPaint.colorFilter = colorFilter; eyePaint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java") override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private companion object {
        const val IDLE_PERIOD_MS = 2600L
        const val BLINK_MS = 180L
        const val HOP_MS = 520L
    }
}
