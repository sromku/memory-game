package com.snatik.matches.ui.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import com.snatik.matches.R
import com.snatik.matches.ui.character.CharacterDrawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The "well done" moment over a finished board: a burst of confetti from the middle of the screen,
 * and a few of the round's characters who come up onto the ground and hop about for as long as
 * the screen is shown. Everything is drawn by this one view; the characters animate themselves.
 */
class CelebrationView(context: Context) : View(context) {

    private class Particle(
        var x: Float, var y: Float, var vx: Float, var vy: Float,
        var angle: Float, val spin: Float, val color: Int, val size: Float, val star: Boolean,
    )

    /** A character on the ground: where it stands, how far it has come up, and when it hops next. */
    private class Guest(val drawable: CharacterDrawable, val x: Float, val size: Int) {
        var entrance = 0f
        var nextHopAt = 0L
    }

    private val particles = mutableListOf<Particle>()
    private val guests = mutableListOf<Guest>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val star = Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) 1f else 0.45f
            val a = -PI / 2 + i * PI / 5
            val x = (cos(a) * r).toFloat()
            val y = (sin(a) * r).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    private val palette = listOf(
        R.color.road_gold, R.color.road_panel, R.color.confetti_red, R.color.white,
        R.color.confetti_green, R.color.confetti_orange, R.color.confetti_pink,
    ).map { ContextCompat.getColor(context, it) }

    private var groundY = 0f
    private var lastFrameAt = 0L
    private var confettiEndsAt = 0L
    private val random = Random(System.nanoTime())

    private val frame = object : Runnable {
        override fun run() {
            val now = SystemClock.uptimeMillis()
            val dt = ((now - lastFrameAt).coerceIn(0, 50)) / 1000f
            lastFrameAt = now
            step(dt, now)
            invalidate()
            if (isAttachedToWindow) postOnAnimation(this)
        }
    }

    /**
     * Brings [characters] up onto the ground at [groundY] (their feet line) and, when [confetti] is
     * set, throws confetti from the centre of the view.
     */
    fun start(characters: List<CharacterDrawable>, groundY: Float, confetti: Boolean) {
        this.groundY = groundY
        val spots = SPOTS.shuffled(random).take(characters.size).sorted()
        val size = (height * GUEST_HEIGHT).toInt()
        guests.clear()
        characters.forEachIndexed { index, drawable ->
            drawable.callback = this
            drawable.start()
            guests += Guest(drawable, spots[index], size).also { guest ->
                ValueAnimator.ofFloat(0f, 1f).apply {
                    startDelay = ENTRANCE_STAGGER_MS * index
                    duration = ENTRANCE_MS
                    interpolator = OvershootInterpolator(1.4f)
                    addUpdateListener { guest.entrance = it.animatedValue as Float }
                    start()
                }
                guest.nextHopAt = SystemClock.uptimeMillis() + ENTRANCE_MS + ENTRANCE_STAGGER_MS * index + random.nextLong(400, 1600)
            }
        }
        if (confetti) burst()
        lastFrameAt = SystemClock.uptimeMillis()
        removeCallbacks(frame)
        postOnAnimation(frame)
    }

    private fun burst() {
        val cx = width / 2f
        val cy = height * BURST_Y
        val unit = height.toFloat()
        repeat(PARTICLE_COUNT) {
            val angle = (-160f + random.nextFloat() * 140f) * PI / 180
            val speed = unit * (0.6f + random.nextFloat() * 1.0f)
            particles += Particle(
                x = cx + (random.nextFloat() - 0.5f) * unit * 0.1f,
                y = cy,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                angle = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 720f,
                color = palette[random.nextInt(palette.size)],
                size = unit * (0.012f + random.nextFloat() * 0.014f),
                star = random.nextInt(4) == 0,
            )
        }
        confettiEndsAt = SystemClock.uptimeMillis() + CONFETTI_MS
    }

    private fun step(dt: Float, now: Long) {
        if (particles.isNotEmpty()) {
            val gravity = height * GRAVITY
            val drag = 1f - DRAG * dt
            for (p in particles) {
                p.vy += gravity * dt
                p.vx *= drag
                p.vy *= drag
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.angle += p.spin * dt
            }
            if (now >= confettiEndsAt) particles.clear()
        }
        for (guest in guests) {
            if (guest.entrance >= 1f && now >= guest.nextHopAt) {
                guest.drawable.hop()
                guest.nextHopAt = now + random.nextLong(HOP_GAP_MIN_MS, HOP_GAP_MAX_MS)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        for (guest in guests) {
            val d = guest.drawable
            val size = guest.size
            val left = (width * guest.x - size / 2f).toInt()
            val feet = groundY + (1f - guest.entrance) * size * 1.3f
            val top = (feet - size * d.feetFraction).toInt()
            d.setBounds(left, top, left + size, top + size)
            d.draw(canvas)
        }
        if (particles.isEmpty()) return
        val fadeFrom = confettiEndsAt - CONFETTI_FADE_MS
        val now = SystemClock.uptimeMillis()
        val alpha = if (now < fadeFrom) 255 else (255 * (confettiEndsAt - now).coerceAtLeast(0) / CONFETTI_FADE_MS.toFloat()).toInt()
        for (p in particles) {
            paint.color = p.color
            paint.alpha = alpha
            canvas.withRotation(p.angle, p.x, p.y) {
                if (p.star) {
                    withTranslation(p.x, p.y) { scale(p.size, p.size); drawPath(star, paint) }
                } else {
                    drawRect(p.x - p.size * 0.7f, p.y - p.size * 0.4f, p.x + p.size * 0.7f, p.y + p.size * 0.4f, paint)
                }
            }
        }
    }

    private inline fun Canvas.withTranslation(x: Float, y: Float, block: Canvas.() -> Unit) {
        val checkpoint = save()
        translate(x, y)
        try { block() } finally { restoreToCount(checkpoint) }
    }

    override fun verifyDrawable(who: Drawable): Boolean = guests.any { it.drawable === who } || super.verifyDrawable(who)

    override fun onDetachedFromWindow() {
        removeCallbacks(frame)
        guests.forEach { it.drawable.stop() }
        super.onDetachedFromWindow()
    }

    private companion object {
        /** Standing spots as fractions of the width, clear of the popup in the middle. */
        val SPOTS = listOf(0.1f, 0.22f, 0.78f, 0.9f)
        const val GUEST_HEIGHT = 0.24f
        const val BURST_Y = 0.5f
        const val PARTICLE_COUNT = 120
        /** Gravity and air drag, in view heights per second squared and per second. */
        const val GRAVITY = 1.0f
        const val DRAG = 1.1f
        const val CONFETTI_MS = 3400L
        const val CONFETTI_FADE_MS = 700L
        const val ENTRANCE_MS = 550L
        const val ENTRANCE_STAGGER_MS = 160L
        const val HOP_GAP_MIN_MS = 1200L
        const val HOP_GAP_MAX_MS = 3200L
    }
}
