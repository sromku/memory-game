package com.snatik.matches.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withScale
import com.snatik.matches.R
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.menu.MenuVisitor
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * The menu backdrop, gently alive: the trees lean a little in the wind (the band of the picture
 * above the grass line is sheared, anchored at the ground) and a few flat clouds drift across the
 * sky. Everything is one bitmap and three shapes per frame, redrawn at a relaxed 30 fps.
 */
class LivingSceneView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var scene: Bitmap? = null
    private var running = false
    private val startMillis = System.currentTimeMillis()

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE6FFFFFF.toInt() }
    private val cloudShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF }
    private val matrix = Matrix()
    private val src = Rect()
    private val dst = RectF()
    private val cloud = Path()

    /** Where the picture's trees and grass line sit, as fractions of its height (see art/original/background.png). */
    private val treeTop = 0.33f
    private val grassLine = 0.825f

    private companion object {
        /** A visitor stands about a fifth of the screen tall. */
        const val VISITOR_HEIGHT = 0.19f
        const val BUBBLE_SECONDS = 2.4f
        const val GREETING_DELAY_SECONDS = 1.5f
    }

    /** An animal standing on the grass: its drawable animates itself; the view only places it and nudges a hop now and then. */
    private class Visitor(val x: Float, val scale: Float, val drawable: CharacterDrawable, var nextHopAt: Float) {
        val box = Rect()
        var phrase: String? = null
        var spokeAt = 0f
        var rimColor = 0
    }

    /** Bubble rim colours, taken from the game's own art: tooltip blue, play-button gold, ribbon orange, tree green, pig pink, monster purple. */
    private val rimColors = intArrayOf(0xFF2EAED9.toInt(), 0xFFF5B400.toInt(), 0xFFF0511E.toInt(), 0xFF5DB822.toInt(), 0xFFF06292.toInt(), 0xFF9C5FD0.toInt())
    private var lastRim = -1

    private val phrases = resources.getStringArray(R.array.animal_phrases)
    private var lastPhrase = -1

    /** The friend of the day's hello: said once, a moment after the scene starts moving. */
    private var greeting: String? = null
    private var greetAt = 0f
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val bubbleRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val bubbleShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x30000000 }
    private val tailPath = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E2D33.toInt()
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.grobold)
    }
    private val bubble = Path()
    private val visitors = mutableListOf<Visitor>()
    private val random = Random(System.nanoTime())

    private class Cloud(val y: Float, val size: Float, val speed: Float, val phase: Float)
    private val clouds = listOf(Cloud(0.07f, 0.085f, 0.014f, 0.15f), Cloud(0.17f, 0.06f, 0.010f, 0.62f), Cloud(0.11f, 0.045f, 0.019f, 0.90f))

    fun setScene(bitmap: Bitmap) {
        scene = bitmap
        invalidate()
    }

    /** Puts animals on the grass; each keeps breathing and blinking through its own drawable. */
    fun setVisitors(placed: List<Pair<MenuVisitor, CharacterDrawable>>) {
        visitors.forEach { it.drawable.stop(); it.drawable.callback = null }
        visitors.clear()
        val now = (System.currentTimeMillis() - startMillis) / 1000f
        for ((visitor, drawable) in placed) {
            drawable.callback = this
            visitors += Visitor(visitor.x, visitor.scale, drawable, now + 2f + random.nextFloat() * 6f)
        }
        if (running) visitors.forEach { it.drawable.start() }
        invalidate()
    }

    /** The first visitor says [text] once, [delaySeconds] after the scene is next started. */
    fun greet(text: String, delaySeconds: Float = GREETING_DELAY_SECONDS) {
        greeting = text
        greetAt = (System.currentTimeMillis() - startMillis) / 1000f + delaySeconds
        if (running) start()
    }

    fun start() {
        running = true
        visitors.forEach { it.drawable.start() }
        greeting?.let { greetAt = maxOf(greetAt, (System.currentTimeMillis() - startMillis) / 1000f + GREETING_DELAY_SECONDS) }
        postInvalidateOnAnimation()
    }

    fun stop() {
        running = false
        visitors.forEach { it.drawable.stop() }
    }

    override fun onDetachedFromWindow() { stop(); super.onDetachedFromWindow() }

    override fun onDraw(canvas: Canvas) {
        val bitmap = scene ?: return
        val t = (System.currentTimeMillis() - startMillis) / 1000f
        // The picture covers the view like ImageView's centerCrop.
        val scale = maxOf(width / bitmap.width.toFloat(), height / bitmap.height.toFloat())
        val drawnW = bitmap.width * scale
        val drawnH = bitmap.height * scale
        val left = (width - drawnW) / 2
        val top = (height - drawnH) / 2

        // 1. The whole scene.
        matrix.reset(); matrix.setScale(scale, scale); matrix.postTranslate(left, top)
        canvas.drawBitmap(bitmap, matrix, paint)

        // 2. Clouds behind the trees: drawn over the sky, then the tree band covers their lower part.
        for (c in clouds) {
            val x = ((t * c.speed + c.phase) % 1.2f) * 1.2f - 0.1f
            drawCloud(canvas, left + x * drawnW, top + c.y * drawnH, c.size * drawnW)
        }

        // 3. The tree band, sheared by a slow breeze and anchored at the grass line.
        val bandTop = (bitmap.height * treeTop).toInt()
        val bandBottom = (bitmap.height * grassLine).toInt()
        src.set(0, bandTop, bitmap.width, bandBottom)
        dst.set(left, top + bandTop * scale, left + drawnW, top + bandBottom * scale)
        val lean = 0.012f * sin(t * 2 * PI / 6.5).toFloat() + 0.004f * sin(t * 2 * PI / 1.7 + 1).toFloat()
        matrix.reset(); matrix.setSkew(lean, 0f, 0f, dst.bottom)
        canvas.withMatrix(matrix) { drawBitmap(bitmap, src, dst, paint) }

        // 4. The animals, feet on the grass line, in front of the trees.
        val grassY = top + grassLine * drawnH
        for (v in visitors) {
            val size = (height * VISITOR_HEIGHT * v.scale).toInt()
            val cx = left + v.x * drawnW
            // Feet on the grass: the character box extends below the feet to hold the shadow.
            val feet = v.drawable.feetFraction * size
            val boxTop = grassY - feet + size * 0.02f
            v.box.set((cx - size / 2).toInt(), boxTop.toInt(), (cx + size / 2).toInt(), (boxTop + size).toInt())
            v.drawable.bounds = v.box
            v.drawable.draw(canvas)
            v.phrase?.let { drawBubble(canvas, v, it, t - v.spokeAt) }
            if (running && t > v.nextHopAt) {
                v.drawable.hop()
                v.nextHopAt = t + 4f + random.nextFloat() * 8f
            }
        }
        val hello = greeting
        if (running && hello != null && t > greetAt && visitors.isNotEmpty()) {
            greeting = null
            speak(visitors.first(), hello)
        }

        if (running) postInvalidateOnAnimation()
    }

    /** Tapping an animal makes it hop and say something. */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return false
        val hit = visitors.lastOrNull { it.box.contains(event.x.toInt(), event.y.toInt()) } ?: return false
        var pick = random.nextInt(phrases.size)
        if (pick == lastPhrase) pick = (pick + 1) % phrases.size
        lastPhrase = pick
        speak(hit, phrases[pick])
        performClick()
        return true
    }

    /** The animal hops and shows [text] in a bubble with a fresh rim colour. */
    private fun speak(v: Visitor, text: String) {
        var rim = random.nextInt(rimColors.size)
        if (rim == lastRim) rim = (rim + 1) % rimColors.size
        lastRim = rim
        v.rimColor = rimColors[rim]
        v.phrase = text
        v.spokeAt = (System.currentTimeMillis() - startMillis) / 1000f
        v.drawable.hop()
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /** A speech bubble above the animal: pops in, holds, fades out. */
    private fun drawBubble(canvas: Canvas, v: Visitor, text: String, age: Float) {
        if (age > BUBBLE_SECONDS) { v.phrase = null; return }
        val pop = (age / 0.18f).coerceAtMost(1f).let { 1.1f - 0.1f * it }.coerceAtLeast(1f) * (if (age < 0.18f) 0.6f + 0.4f * (age / 0.18f) else 1f)
        val alpha = if (age > BUBBLE_SECONDS - 0.4f) ((BUBBLE_SECONDS - age) / 0.4f).coerceIn(0f, 1f) else 1f
        textPaint.textSize = height * 0.045f
        bubbleRim.color = v.rimColor
        textPaint.alpha = (255 * alpha).toInt(); bubblePaint.alpha = (255 * alpha).toInt(); bubbleRim.alpha = (255 * alpha).toInt()
        bubbleShadow.alpha = (0x30 * alpha).toInt()
        bubbleRim.strokeWidth = height * 0.0065f
        val padX = textPaint.textSize * 0.7f
        val bw = textPaint.measureText(text) + 2 * padX
        val bh = textPaint.textSize * 1.9f
        val tail = bh * 0.35f
        val margin = height * 0.025f // bubbles of animals at the edges stay comfortably on screen
        val cx = v.box.exactCenterX().coerceIn(bw / 2 + margin, width - bw / 2 - margin)
        val bottom = v.box.top - tail - height * 0.01f
        val r = bh * 0.45f
        canvas.withScale(pop, pop, v.box.exactCenterX(), bottom + tail) {
            // One outline around body and tail: the rounded box and a curved tail are merged into a single path.
            bubble.rewind()
            bubble.addRoundRect(cx - bw / 2, bottom - bh, cx + bw / 2, bottom, r, r, Path.Direction.CW)
            val tx = v.box.exactCenterX().coerceIn(cx - bw / 2 + r, cx + bw / 2 - r)
            tailPath.rewind()
            tailPath.moveTo(tx - tail * 0.7f, bottom - r * 0.5f)
            tailPath.quadTo(tx - tail * 0.2f, bottom + tail * 0.45f, tx + tail * 0.05f, bottom + tail)
            tailPath.quadTo(tx + tail * 0.3f, bottom + tail * 0.35f, tx + tail * 0.7f, bottom - r * 0.5f)
            tailPath.close()
            bubble.op(tailPath, Path.Op.UNION)
            canvas.withTranslation(0f, height * 0.006f) { drawPath(bubble, bubbleShadow) }
            drawPath(bubble, bubblePaint)
            drawPath(bubble, bubbleRim)
            drawText(text, cx, bottom - bh / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        }
    }

    /** A flat cartoon cloud: four overlapping circles on a flat base, in the style of the game's art. */
    private fun drawCloud(canvas: Canvas, x: Float, y: Float, w: Float) {
        val h = w * 0.55f
        cloud.rewind()
        cloud.addRoundRect(x, y + h * 0.45f, x + w, y + h, h * 0.25f, h * 0.25f, Path.Direction.CW)
        cloud.addCircle(x + w * 0.28f, y + h * 0.5f, h * 0.42f, Path.Direction.CW)
        cloud.addCircle(x + w * 0.52f, y + h * 0.38f, h * 0.5f, Path.Direction.CW)
        cloud.addCircle(x + w * 0.74f, y + h * 0.52f, h * 0.38f, Path.Direction.CW)
        canvas.withTranslation(0f, h * 0.08f) { drawPath(cloud, cloudShadow) }
        canvas.drawPath(cloud, cloudPaint)
    }
}
