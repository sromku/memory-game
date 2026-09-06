package com.snatik.matches.ui.game

import android.graphics.Camera
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Rotates a view around its vertical axis, swapping [from] for [to] halfway through so the back
 * face is never shown mirrored.
 */
class FlipAnimation(
    private val from: View,
    private val to: View,
    private val forward: Boolean,
) : Animation() {

    private var centerX = 0f
    private var centerY = 0f
    private val camera = Camera()

    init {
        duration = 700
        fillAfter = false
        interpolator = AccelerateDecelerateInterpolator()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        centerX = width / 2f
        centerY = height / 2f
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        var degrees = 180f * interpolatedTime
        if (interpolatedTime >= 0.5f) {
            degrees -= 180f
            from.visibility = View.GONE
            to.visibility = View.VISIBLE
        }
        if (forward) degrees = -degrees

        val matrix = t.matrix
        camera.save()
        camera.rotateY(degrees)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-centerX, -centerY)
        matrix.postTranslate(centerX, centerY)
    }
}
