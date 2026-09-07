package com.snatik.matches.ui.road

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.snatik.matches.R
import com.snatik.matches.game.progression.RoundSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A road of rounds winding from left to right. Done rounds carry their stars and can be replayed,
 * the next round glows under a bobbing arrow, the rest are still asleep. Scrolls and flings
 * sideways; tapping a playable round reports it through [onRoundSelected].
 *
 * The view only paints: positions come from [RoadGeometry], states from [RoadNode].
 */
class RoadMapView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var onRoundSelected: ((RoundSpec) -> Unit)? = null

    private var nodes: List<RoadNode> = emptyList()
    private var geometry: RoadGeometry? = null
    private var nextIndex: Int? = null

    /** What to bring into view once the road is laid out. */
    private class Focus(val index: Int, val celebrate: Boolean)

    private var pendingFocus: Focus? = null

    private val density = resources.displayMetrics.density
    private val radius = resources.getDimension(R.dimen.road_node_radius)
    private val spacing = resources.getDimension(R.dimen.road_spacing)
    private val margin = resources.getDimension(R.dimen.road_margin)

    private fun color(id: Int) = ContextCompat.getColor(context, id)

    private val roadEdgePaint = strokePaint(color(R.color.road_edge), radius * 0.95f)
    private val roadSurfacePaint = strokePaint(color(R.color.road_surface), radius * 0.6f).apply {
        pathEffect = DashPathEffect(floatArrayOf(radius * 0.55f, radius * 0.45f), 0f)
    }
    private val roadDonePaint = strokePaint(color(R.color.road_gold), radius * 0.6f)
    private val shadowPaint = fillPaint(color(R.color.road_shadow))
    private val nodeFillPaint = fillPaint(0)
    private val nodeRimPaint = strokePaint(0, radius * 0.13f)
    private val starPaint = fillPaint(color(R.color.road_gold))
    private val starRimPaint = strokePaint(color(R.color.road_gold_dark), radius * 0.06f)
    private val slotPaint = fillPaint(color(R.color.road_star_slot))
    private val slotRimPaint = strokePaint(color(R.color.road_star_slot_rim), radius * 0.05f)
    private val haloPaint = strokePaint(color(R.color.white), 3f * density)
    private val markerPaint = fillPaint(color(R.color.white))
    private val markerRimPaint = strokePaint(color(R.color.road_rim), radius * 0.1f)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = color(R.color.white)
        typeface = ResourcesCompat.getFont(context, R.font.grobold)
        textAlign = Paint.Align.CENTER
        textSize = radius * 0.95f
        setShadowLayer(radius * 0.1f, 0f, radius * 0.07f, color(R.color.text_shadow))
    }
    private val textOffset = -(textPaint.ascent() + textPaint.descent()) / 2f

    private val roadPath = Path()
    private val donePath = Path()
    private val starUnit = starPath(points = 5, inner = 0.48f)
    private val badgeUnit = starPath(points = 8, inner = 0.86f)
    private val scratch = Path()
    private val matrix = Matrix()

    private val scroller = OverScroller(context)
    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (!scroller.isFinished) scroller.abortAnimation()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            moveTo(scrollX + distanceX)
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val geometry = geometry ?: return false
            scroller.fling(scrollX, 0, (-velocityX).toInt(), 0, 0, geometry.maxScroll(width), 0, 0, radius.toInt(), 0)
            postInvalidateOnAnimation()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            tap(e.x + scrollX, e.y)
            return true
        }
    })

    /** 0..1, looping: drives the glow and the arrow of the next round. */
    private var phase = 0f
    private val pulse = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = PULSE_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { phase = it.animatedValue as Float; invalidate() }
    }

    /** The round whose stars are popping in, and how far the pop has come. */
    private var celebratedIndex: Int? = null
    private var popProgress = 1f
    private var popAnimator: ValueAnimator? = null

    /** A locked round that was tapped shakes its head. */
    private var wobbleIndex: Int? = null
    private var wobbleAngle = 0f
    private var wobbleAnimator: ValueAnimator? = null

    init {
        contentDescription = context.getString(R.string.cd_road_map)
    }

    /**
     * Shows a road. When [celebrate] names a round that is now done, the map opens on it, pops its
     * stars in, and then travels on to the next round.
     */
    fun show(nodes: List<RoadNode>, celebrate: RoundSpec? = null) {
        this.nodes = nodes
        nextIndex = nodes.firstOrNull { it.state == RoadNode.State.NEXT }?.round?.index
        val celebrated = celebrate?.index?.takeIf { nodes.getOrNull(it - 1)?.state == RoadNode.State.DONE }
        pendingFocus = Focus(index = celebrated ?: nextIndex ?: nodes.size, celebrate = celebrated != null)
        if (width > 0 && height > 0) layoutRoad()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (nodes.isNotEmpty()) layoutRoad()
    }

    private fun layoutRoad() {
        val geometry = RoadGeometry(
            roundCount = nodes.size,
            spacing = spacing,
            margin = margin,
            centerY = height * CENTER_Y,
            amplitude = height * AMPLITUDE,
        )
        this.geometry = geometry
        buildPaths(geometry)
        pendingFocus?.let { focus ->
            pendingFocus = null
            scrollTo(geometry.scrollToCenter(focus.index, width), 0)
            if (focus.celebrate) celebrate(focus.index, geometry)
        }
    }

    /** The road as smooth S-curves between rounds; the gold part covers what is done. */
    private fun buildPaths(geometry: RoadGeometry) {
        roadPath.rewind()
        donePath.rewind()
        val doneUntil = (nextIndex ?: (nodes.size + 1)) - 1
        roadPath.moveTo(geometry.x(1), geometry.y(1))
        donePath.moveTo(geometry.x(1), geometry.y(1))
        for (index in 2..nodes.size) {
            val x0 = geometry.x(index - 1)
            val y0 = geometry.y(index - 1)
            val x1 = geometry.x(index)
            val y1 = geometry.y(index)
            val bend = spacing * 0.5f
            roadPath.cubicTo(x0 + bend, y0, x1 - bend, y1, x1, y1)
            if (index <= doneUntil) donePath.cubicTo(x0 + bend, y0, x1 - bend, y1, x1, y1)
        }
    }

    private fun celebrate(index: Int, geometry: RoadGeometry) {
        popAnimator?.cancel()
        celebratedIndex = index
        popProgress = 0f
        popAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            startDelay = POP_DELAY_MS
            duration = POP_MS
            interpolator = OvershootInterpolator(2.5f)
            addUpdateListener { popProgress = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    popProgress = 1f
                    nextIndex?.let { next -> postDelayed({ smoothScrollTo(geometry.scrollToCenter(next, width)) }, TRAVEL_DELAY_MS) }
                }
            })
            start()
        }
    }

    private fun tap(x: Float, y: Float) {
        val geometry = geometry ?: return
        val index = geometry.roundAt(x, y, radius * TAP_REACH) ?: return
        val node = nodes[index - 1]
        if (node.isPlayable) onRoundSelected?.invoke(node.round) else wobble(index)
    }

    private fun wobble(index: Int) {
        wobbleAnimator?.cancel()
        wobbleIndex = index
        wobbleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = WOBBLE_MS
            interpolator = LinearInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                wobbleAngle = WOBBLE_DEGREES * sin(t * 3 * PI).toFloat() * (1 - t)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { wobbleIndex = null; wobbleAngle = 0f }
            })
            start()
        }
    }

    private fun moveTo(x: Float) {
        val geometry = geometry ?: return
        scrollTo(x.toInt().coerceIn(0, geometry.maxScroll(width)), 0)
    }

    private fun smoothScrollTo(x: Int) {
        scroller.startScroll(scrollX, 0, x - scrollX, 0, TRAVEL_MS)
        postInvalidateOnAnimation()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) performClick()
        return gestures.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val geometry = geometry ?: return
        canvas.drawPath(roadPath, roadEdgePaint)
        canvas.drawPath(roadPath, roadSurfacePaint)
        if (!donePath.isEmpty) canvas.drawPath(donePath, roadDonePaint)

        val left = scrollX.toFloat()
        val reach = radius * 2.5f
        for (index in geometry.roundsWithin(left, left + width, reach)) {
            drawNode(canvas, index, nodes[index - 1], geometry.x(index), geometry.y(index))
        }
        nextIndex?.let { if (it in geometry.roundsWithin(left, left + width, reach)) drawMarker(canvas, geometry.x(it), geometry.y(it), nodes[it - 1]) }
    }

    private fun nodeRadius(node: RoadNode): Float {
        val special = if (node.round.isSpecial) SPECIAL_SCALE else 1f
        val breathing = if (node.state == RoadNode.State.NEXT) NEXT_SCALE + 0.04f * sin(phase * 2 * PI).toFloat() else 1f
        return radius * special * breathing
    }

    private fun drawNode(canvas: Canvas, index: Int, node: RoadNode, cx: Float, cy: Float) {
        val r = nodeRadius(node)
        canvas.withSave {
            if (index == wobbleIndex) rotate(wobbleAngle, cx, cy)
            if (node.state == RoadNode.State.NEXT) {
                haloPaint.alpha = ((1 - phase) * 200).toInt()
                drawCircle(cx, cy, r * (1.15f + 0.45f * phase), haloPaint)
            }
            drawShape(this, node, cx + 2 * density, cy + 3 * density, r, shadowPaint)
            nodeFillPaint.color = color(
                when (node.state) {
                    RoadNode.State.DONE -> R.color.road_panel
                    RoadNode.State.NEXT -> R.color.road_gold
                    RoadNode.State.LOCKED -> R.color.road_locked
                },
            )
            nodeRimPaint.color = color(if (node.state == RoadNode.State.LOCKED) R.color.road_locked_rim else R.color.road_rim)
            drawShape(this, node, cx, cy, r, nodeFillPaint)
            drawShape(this, node, cx, cy, r, nodeRimPaint)
            textPaint.alpha = if (node.state == RoadNode.State.LOCKED) LOCKED_TEXT_ALPHA else 255
            drawText(if (node.round.isSpecial) SPECIAL_LABEL else index.toString(), cx, cy + textOffset, textPaint)
            if (node.state == RoadNode.State.DONE) drawStars(this, node, cx, cy + r + radius * 0.5f, if (index == celebratedIndex) popProgress else 1f)
        }
    }

    private fun drawShape(canvas: Canvas, node: RoadNode, cx: Float, cy: Float, r: Float, paint: Paint) {
        if (node.round.isSpecial) drawUnit(canvas, badgeUnit, cx, cy, r, paint) else canvas.drawCircle(cx, cy, r, paint)
    }

    private fun drawStars(canvas: Canvas, node: RoadNode, cx: Float, cy: Float, pop: Float) {
        for (slot in 0 until MAX_STARS) {
            val sx = cx + (slot - 1) * radius * 0.72f
            if (slot < node.stars) {
                val size = radius * 0.36f * pop
                if (size <= 0f) continue
                drawUnit(canvas, starUnit, sx, cy, size, starPaint)
                drawUnit(canvas, starUnit, sx, cy, size, starRimPaint)
            } else {
                canvas.drawCircle(sx, cy, radius * 0.19f, slotPaint)
                canvas.drawCircle(sx, cy, radius * 0.19f, slotRimPaint)
            }
        }
    }

    /** A white arrow bobbing above the next round. */
    private fun drawMarker(canvas: Canvas, cx: Float, cy: Float, node: RoadNode) {
        val bob = radius * 0.22f * (0.5f + 0.5f * sin(phase * 2 * PI).toFloat())
        val tipY = cy - nodeRadius(node) - radius * 0.45f - bob
        val w = radius * 0.55f
        val h = radius * 0.65f
        scratch.rewind()
        scratch.moveTo(cx, tipY)
        scratch.lineTo(cx - w, tipY - h)
        scratch.lineTo(cx - w * 0.45f, tipY - h)
        scratch.lineTo(cx - w * 0.45f, tipY - h * 1.8f)
        scratch.lineTo(cx + w * 0.45f, tipY - h * 1.8f)
        scratch.lineTo(cx + w * 0.45f, tipY - h)
        scratch.lineTo(cx + w, tipY - h)
        scratch.close()
        canvas.drawPath(scratch, markerPaint)
        canvas.drawPath(scratch, markerRimPaint)
    }

    private fun drawUnit(canvas: Canvas, unit: Path, cx: Float, cy: Float, scale: Float, paint: Paint) {
        matrix.setScale(scale, scale)
        matrix.postTranslate(cx, cy)
        unit.transform(matrix, scratch)
        canvas.drawPath(scratch, paint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimation()
    }

    override fun onDetachedFromWindow() {
        pulse.cancel()
        popAnimator?.cancel()
        wobbleAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateAnimation()
    }

    private fun updateAnimation() {
        val shouldRun = isAttachedToWindow && windowVisibility == VISIBLE
        if (shouldRun && !pulse.isStarted) pulse.start() else if (!shouldRun) pulse.cancel()
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun strokePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = color
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** A star with [points] tips on the unit circle and valleys at [inner], the first tip pointing up. */
    private fun starPath(points: Int, inner: Float): Path {
        val path = Path()
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) 1f else inner
            val angle = -PI / 2 + i * PI / points
            val x = (cos(angle) * r).toFloat()
            val y = (sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private companion object {
        const val MAX_STARS = 3
        /** Special rounds are the mini-game; the map keeps that a surprise. */
        const val SPECIAL_LABEL = "?"
        const val CENTER_Y = 0.52f
        const val AMPLITUDE = 0.17f
        const val SPECIAL_SCALE = 1.3f
        const val NEXT_SCALE = 1.14f
        const val TAP_REACH = 1.6f
        const val LOCKED_TEXT_ALPHA = 170
        const val WOBBLE_DEGREES = 10f
        const val PULSE_MS = 1400L
        const val POP_DELAY_MS = 400L
        const val POP_MS = 600L
        const val TRAVEL_DELAY_MS = 250L
        const val TRAVEL_MS = 700
        const val WOBBLE_MS = 450L
    }
}
