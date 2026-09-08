package com.snatik.matches.ui.minigame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import androidx.core.graphics.withClip
import com.snatik.matches.R
import com.snatik.matches.ui.character.CharacterDrawable
import kotlin.math.PI
import kotlin.math.sin

/**
 * The party of "Who was here?": characters standing in a row on the ground, who pop up out of it,
 * drop back into it, and leave a bobbing "?" where one of them is missing. The view places and
 * animates; the characters idle and hop on their own.
 */
class PartySceneView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    /** One character: where it stands (fraction of the width) and how far up out of the ground it is. */
    private class Guest(val drawable: CharacterDrawable, val x: Float) {
        var visible = 0f
        var animator: ValueAnimator? = null

        /** 1 while the character sings, fading to 0. */
        var glow = 0f
        var glowAnimator: ValueAnimator? = null

        /** Side-to-side offset while shaking its head, -1..1. */
        var shake = 0f
        var shakeAnimator: ValueAnimator? = null
        val bounds = Rect()
    }

    /** Set to receive taps on characters (their 0-based position); null leaves the scene passive. */
    var onGuestTapped: ((Int) -> Unit)? = null

    private val guests = mutableListOf<Guest>()
    private var groundY = 0f
    private var size = 0
    private var gap: Int? = null
    private var markerPhase = 0f

    private val markerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = MARKER_PERIOD_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { markerPhase = it.animatedValue as Float; invalidate() }
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.road_gold) }
    private val markerFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.road_gold) }
    private val markerRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.road_rim)
    }
    private val markerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        typeface = ResourcesCompat.getFont(context, R.font.game)
        isFakeBoldText = resources.getBoolean(R.bool.game_text_fake_bold)
        textAlign = Paint.Align.CENTER
    }

    /** Each character is a virtual view for screen readers; they can be tapped when the scene takes taps. */
    private val accessibility = object : ExploreByTouchHelper(this) {
        override fun getVirtualViewAt(x: Float, y: Float): Int {
            val hit = guests.indexOfFirst { it.visible > 0.5f && it.bounds.contains(x.toInt(), y.toInt()) }
            return if (hit >= 0) hit else INVALID_ID
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
            guests.forEachIndexed { index, guest -> if (guest.visible > 0.5f) virtualViewIds += index }
        }

        override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
            val guest = guests.getOrNull(virtualViewId)
            node.contentDescription = context.getString(R.string.cd_friend, virtualViewId + 1)
            node.setBoundsInParent(if (guest != null && !guest.bounds.isEmpty) Rect(guest.bounds) else Rect(0, 0, 1, 1))
            if (onGuestTapped != null) node.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK)
        }

        override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            if (action != AccessibilityNodeInfoCompat.ACTION_CLICK) return false
            val listener = onGuestTapped ?: return false
            if (virtualViewId !in guests.indices) return false
            listener(virtualViewId)
            return true
        }
    }

    init {
        ViewCompat.setAccessibilityDelegate(this, accessibility)
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean =
        accessibility.dispatchHoverEvent(event) || super.dispatchHoverEvent(event)

    fun setGround(y: Float) {
        groundY = y
        invalidate()
    }

    /** A new party, everyone still hidden in the ground, spread evenly around the middle. */
    fun setParty(drawables: List<CharacterDrawable>) {
        clear()
        val n = drawables.size
        val spacing = if (n <= 4) WIDE_SPACING else TIGHT_SPACING
        size = (height * (if (n <= 4) TALL_GUEST else SHORT_GUEST)).toInt()
        drawables.forEachIndexed { index, drawable ->
            drawable.callback = this
            drawable.start()
            guests += Guest(drawable, 0.5f + (index - (n - 1) / 2f) * spacing)
        }
        accessibility.invalidateRoot()
        invalidate()
    }

    /** Everyone pops up, one after another, and hops once they are out. */
    fun enter(except: Int? = null) {
        var order = 0
        guests.forEachIndexed { index, guest ->
            if (index == except) return@forEachIndexed
            rise(guest, delay = ENTER_STAGGER_MS * order++)
        }
    }

    /** Everyone drops into the ground. */
    fun hide() {
        gap = null
        markerAnimator.cancel()
        guests.forEachIndexed { index, guest -> sink(guest, delay = HIDE_STAGGER_MS * index) }
    }

    /** Marks where the missing one stood. */
    fun showGap(position: Int) {
        gap = position
        markerAnimator.start()
    }

    /** The missing one is back: it pops into its spot and everyone hops for joy. */
    fun fillGap() {
        val position = gap ?: return
        gap = null
        markerAnimator.cancel()
        rise(guests[position], delay = 0)
        guests.forEachIndexed { index, guest ->
            if (index != position) postDelayed({ guest.drawable.hop() }, JOY_DELAY_MS + JOY_STAGGER_MS * index)
        }
    }

    /** The character at [position] sings: a glow blooms behind it and it hops. */
    fun spotlight(position: Int, durationMs: Long) {
        val guest = guests.getOrNull(position) ?: return
        guest.glowAnimator?.cancel()
        guest.drawable.hop()
        guest.glowAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = durationMs
            interpolator = AccelerateInterpolator(1.2f)
            addUpdateListener { guest.glow = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val listener = onGuestTapped ?: return false
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x.toInt()
        val y = event.y.toInt()
        val hit = guests.indexOfFirst { it.visible > 0.5f && it.bounds.contains(x, y) }
        if (hit >= 0) {
            performClick()
            listener(hit)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /** Everyone hops, one after another. */
    fun celebrate() {
        guests.forEachIndexed { index, guest -> postDelayed({ guest.drawable.hop() }, JOY_STAGGER_MS * index) }
    }

    /** The character at [position] shakes its head: a quick side-to-side wiggle. */
    fun shakeHead(position: Int) {
        val guest = guests.getOrNull(position) ?: return
        guest.shakeAnimator?.cancel()
        guest.shakeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SHAKE_MS
            interpolator = LinearInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                guest.shake = sin(t * 3 * PI).toFloat() * (1 - t)
                invalidate()
            }
            start()
        }
    }

    fun clear() {
        guests.forEach { it.animator?.cancel(); it.glowAnimator?.cancel(); it.shakeAnimator?.cancel(); it.drawable.stop(); it.drawable.callback = null }
        guests.clear()
        gap = null
        markerAnimator.cancel()
        invalidate()
    }

    private fun rise(guest: Guest, delay: Long) {
        guest.animator?.cancel()
        guest.animator = ValueAnimator.ofFloat(guest.visible, 1f).apply {
            startDelay = delay
            duration = RISE_MS
            interpolator = OvershootInterpolator(1.3f)
            addUpdateListener { guest.visible = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = guest.drawable.hop()
            })
            start()
        }
    }

    private fun sink(guest: Guest, delay: Long) {
        guest.animator?.cancel()
        guest.animator = ValueAnimator.ofFloat(guest.visible, 0f).apply {
            startDelay = delay
            duration = SINK_MS
            interpolator = AccelerateInterpolator(1.6f)
            addUpdateListener { guest.visible = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (guests.isEmpty()) return
        // The ground swallows whoever sinks into it.
        canvas.withClip(0f, 0f, width.toFloat(), groundY + size * FEET_SLACK) {
            for (guest in guests) {
                if (guest.visible <= 0f) continue
                val drawable = guest.drawable
                val left = (width * guest.x - size / 2f + guest.shake * size * SHAKE_AMPLITUDE).toInt()
                val feet = groundY + (1f - guest.visible) * size * SINK_DEPTH
                val top = (feet - size * drawable.feetFraction).toInt()
                guest.bounds.set(left, top, left + size, top + size)
                if (guest.glow > 0f) {
                    val cx = left + size / 2f
                    val cy = feet - size * drawable.feetFraction / 2f
                    glowPaint.alpha = (GLOW_ALPHA * guest.glow).toInt()
                    canvas.drawCircle(cx, cy, size * (GLOW_RADIUS + 0.12f * (1 - guest.glow)), glowPaint)
                    glowPaint.alpha = (GLOW_ALPHA * 0.6f * guest.glow).toInt()
                    canvas.drawCircle(cx, cy, size * (GLOW_RADIUS + 0.28f * (1 - guest.glow)), glowPaint)
                }
                drawable.bounds = guest.bounds
                drawable.draw(canvas)
            }
        }
        gap?.let { drawMarker(canvas, guests[it]) }
    }

    private fun drawMarker(canvas: Canvas, guest: Guest) {
        val radius = size * MARKER_RADIUS
        val bob = size * MARKER_BOB * sin(markerPhase * 2 * PI).toFloat()
        val cx = width * guest.x
        val cy = groundY - size * MARKER_HEIGHT - bob
        markerRim.strokeWidth = radius * 0.14f
        markerText.textSize = radius * 1.5f
        canvas.drawCircle(cx, cy, radius, markerFill)
        canvas.drawCircle(cx, cy, radius, markerRim)
        canvas.drawText("?", cx, cy - (markerText.ascent() + markerText.descent()) / 2f, markerText)
    }

    override fun verifyDrawable(who: Drawable): Boolean = guests.any { it.drawable === who } || super.verifyDrawable(who)

    override fun onDetachedFromWindow() {
        guests.forEach { it.animator?.cancel(); it.glowAnimator?.cancel(); it.drawable.stop() }
        markerAnimator.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val WIDE_SPACING = 0.17f
        const val TIGHT_SPACING = 0.14f
        const val TALL_GUEST = 0.27f
        const val SHORT_GUEST = 0.23f
        const val SINK_DEPTH = 1.3f
        const val FEET_SLACK = 0.08f
        const val GLOW_RADIUS = 0.42f
        const val GLOW_ALPHA = 150
        const val MARKER_RADIUS = 0.2f
        const val MARKER_HEIGHT = 0.55f
        const val MARKER_BOB = 0.06f
        const val MARKER_PERIOD_MS = 1300L
        const val RISE_MS = 550L
        const val SINK_MS = 380L
        const val ENTER_STAGGER_MS = 150L
        const val HIDE_STAGGER_MS = 60L
        const val JOY_DELAY_MS = 350L
        const val JOY_STAGGER_MS = 90L
        const val SHAKE_MS = 450L
        const val SHAKE_AMPLITUDE = 0.08f
    }
}
