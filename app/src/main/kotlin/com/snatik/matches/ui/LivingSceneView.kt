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
import android.view.View
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import kotlin.math.PI
import kotlin.math.sin

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

    private class Cloud(val y: Float, val size: Float, val speed: Float, val phase: Float)
    private val clouds = listOf(Cloud(0.07f, 0.085f, 0.014f, 0.15f), Cloud(0.17f, 0.06f, 0.010f, 0.62f), Cloud(0.11f, 0.045f, 0.019f, 0.90f))

    fun setScene(bitmap: Bitmap) {
        scene = bitmap
        invalidate()
    }

    fun start() { running = true; postInvalidateOnAnimation() }
    fun stop() { running = false }

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

        if (running) postInvalidateOnAnimation()
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
