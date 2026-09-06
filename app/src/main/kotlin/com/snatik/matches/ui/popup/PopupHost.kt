package com.snatik.matches.ui.popup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isNotEmpty
import com.snatik.matches.R
import com.snatik.matches.game.GameResult

/**
 * Shows at most one popup over the game inside [container], with the scale-in and scale-out
 * animations the game has always had.
 */
class PopupHost(
    private val container: FrameLayout,
    private val onShownChanged: (shown: Boolean) -> Unit,
) {
    private val context get() = container.context
    private var closing = false

    val isShown: Boolean get() = container.isNotEmpty()

    val isWonShown: Boolean get() = container.children.any { it is PopupWonView }

    fun showSettings(soundEnabled: Boolean, onToggleSound: () -> Boolean, onRate: () -> Unit) {
        reset()
        val scrim = View(context).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.popup_scrim))
            isClickable = true // swallow taps so the menu underneath stays inert
        }
        container.addView(scrim, FrameLayout.LayoutParams(MATCH, MATCH))

        val popup = PopupSettingsView(context, soundEnabled, onToggleSound, onRate)
        container.addView(popup, centered(R.dimen.popup_settings_width, R.dimen.popup_settings_height))
        onShownChanged(true)

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(popup, View.SCALE_X, 0f, 1f),
                ObjectAnimator.ofFloat(popup, View.SCALE_Y, 0f, 1f),
                ObjectAnimator.ofFloat(scrim, View.ALPHA, 0f, 1f),
            )
            duration = OPEN_DURATION_MS
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    fun showWon(result: GameResult, onStar: () -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
        reset()
        val popup = PopupWonView(context, onBack, onNext)
        container.addView(popup, centered(R.dimen.popup_won_width, R.dimen.popup_won_height))
        onShownChanged(true)
        popup.showResult(result, onStar)

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(popup, View.SCALE_X, 0f, 1f),
                ObjectAnimator.ofFloat(popup, View.SCALE_Y, 0f, 1f),
            )
            duration = OPEN_DURATION_MS
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    fun close() {
        if (!isShown || closing) return
        closing = true
        val views = container.children.toList()
        val popup = views.last()
        val scrim = views.dropLast(1).firstOrNull()
        val animators = mutableListOf<Animator>(
            ObjectAnimator.ofFloat(popup, View.SCALE_X, 0f),
            ObjectAnimator.ofFloat(popup, View.SCALE_Y, 0f),
        )
        if (scrim != null) animators += ObjectAnimator.ofFloat(scrim, View.ALPHA, 0f)
        AnimatorSet().apply {
            playTogether(animators)
            duration = CLOSE_DURATION_MS
            interpolator = AccelerateInterpolator(2f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = reset()
            })
            start()
        }
    }

    private fun reset() {
        closing = false
        if (container.isNotEmpty()) {
            container.removeAllViews()
            onShownChanged(false)
        }
    }

    private fun centered(widthRes: Int, heightRes: Int) = FrameLayout.LayoutParams(
        context.resources.getDimensionPixelSize(widthRes),
        context.resources.getDimensionPixelSize(heightRes),
        Gravity.CENTER,
    )

    private companion object {
        const val MATCH = FrameLayout.LayoutParams.MATCH_PARENT
        const val OPEN_DURATION_MS = 500L
        const val CLOSE_DURATION_MS = 300L
    }
}
