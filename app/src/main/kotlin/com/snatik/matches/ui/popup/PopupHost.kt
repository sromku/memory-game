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
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.snatik.matches.ui.image.loadDrawable
import com.snatik.matches.ui.image.warmDrawables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import com.snatik.matches.R
import com.snatik.matches.game.GameResult

/**
 * Shows at most one popup over the game inside [container], with the scale-in and scale-out
 * animations the game has always had.
 */
class PopupHost(
    private val container: FrameLayout,
    private val scope: CoroutineScope,
    private val onShownChanged: (shown: Boolean) -> Unit,
) {
    /** What is on screen: the popup itself, and the views that scale away with it when it closes. */
    private class Shown(val popup: View, val scrim: View?, val companions: List<View>)

    private val context get() = container.context
    private var shown: Shown? = null

    init {
        // The settings popup's close button overhangs its corner.
        container.clipChildren = false
        container.clipToPadding = false
    }
    private var closing = false

    val isShown: Boolean get() = shown != null

    val isWonShown: Boolean get() = shown?.popup is PopupWonView

    /** Popups carry traced vector frames with hundreds of paths; they are inflated off the main thread first. */
    fun showSettings(soundEnabled: Boolean, onToggleSound: () -> Boolean, onRate: () -> Unit, onPrivacyPolicy: () -> Unit) {
        scope.launch {
            val frame = context.loadDrawable(R.drawable.settings_popup)
            val icons = listOf(R.drawable.button_music_on, R.drawable.button_music_off, R.drawable.button_rate).map { context.loadDrawable(it) }
            showSettings(frame, icons, soundEnabled, onToggleSound, onRate, onPrivacyPolicy)
        }
    }

    private fun showSettings(frame: Drawable, icons: List<Drawable>, soundEnabled: Boolean, onToggleSound: () -> Boolean, onRate: () -> Unit, onPrivacyPolicy: () -> Unit) {
        reset()
        val scrim = View(context).apply {
            alpha = 0f
            setBackgroundColor(ContextCompat.getColor(context, R.color.popup_scrim))
            setOnClickListener { close() } // tapping outside the popup dismisses it
        }
        container.addView(scrim, FrameLayout.LayoutParams(MATCH, MATCH))

        val popup = PopupSettingsView(context, frame, icons[0], icons[1], icons[2], soundEnabled, onToggleSound, onRate, onPrivacyPolicy)
        val popupParams = centered(R.dimen.popup_settings_width, R.dimen.popup_settings_height)

        // The popup and its close button live in one holder that is scaled as a whole, so the button
        // stays glued to the popup's corner throughout the open and close animations.
        val closeSize = context.resources.getDimensionPixelSize(R.dimen.popup_close_size)
        val close = ImageView(context).apply {
            setImageResource(R.drawable.ic_popup_close)
            contentDescription = context.getString(R.string.cd_close)
            // Centred on the frame's top-right corner. The ribbon rises above the frame, so the frame's
            // top edge sits at 19% of the popup's height (measured in art/original/settings_popup.png).
            translationX = closeSize / 2f
            translationY = popupParams.height * SETTINGS_FRAME_TOP - closeSize / 2f
            setOnClickListener { close() }
        }
        val holder = FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
            addView(popup, FrameLayout.LayoutParams(popupParams.width, popupParams.height))
            addView(close, FrameLayout.LayoutParams(closeSize, closeSize, Gravity.TOP or Gravity.END))
        }
        container.addView(holder, FrameLayout.LayoutParams(popupParams.width, popupParams.height, Gravity.CENTER))
        present(Shown(holder, scrim, emptyList()))
    }

    fun showWon(result: GameResult, onStar: () -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
        scope.launch {
            val frame = context.loadDrawable(R.drawable.level_complete)
            context.warmDrawables(R.drawable.button_back, R.drawable.button_again, R.drawable.level_complete_star)
            showWon(frame, result, onStar, onBack, onNext)
        }
    }

    private fun showWon(frame: Drawable, result: GameResult, onStar: () -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
        reset()
        val popup = PopupWonView(context, frame, onBack, onNext)
        container.addView(popup, centered(R.dimen.popup_won_width, R.dimen.popup_won_height))
        popup.showResult(result, onStar)
        present(Shown(popup, scrim = null, companions = emptyList()))
    }

    fun close() {
        val current = shown ?: return
        if (closing) return
        closing = true
        AnimatorSet().apply {
            playTogether(scaleAnimators(current, 0f) + listOfNotNull(current.scrim?.let { ObjectAnimator.ofFloat(it, View.ALPHA, 0f) }))
            duration = CLOSE_DURATION_MS
            interpolator = AccelerateInterpolator(2f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = reset()
            })
            start()
        }
    }

    private fun present(next: Shown) {
        shown = next
        onShownChanged(true)
        (listOf(next.popup) + next.companions).forEach { it.scaleX = 0f; it.scaleY = 0f }
        AnimatorSet().apply {
            playTogether(scaleAnimators(next, 1f) + listOfNotNull(next.scrim?.let { ObjectAnimator.ofFloat(it, View.ALPHA, 1f) }))
            duration = OPEN_DURATION_MS
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    private fun scaleAnimators(shown: Shown, to: Float): List<Animator> =
        (listOf(shown.popup) + shown.companions).flatMap { view ->
            listOf(ObjectAnimator.ofFloat(view, View.SCALE_X, to), ObjectAnimator.ofFloat(view, View.SCALE_Y, to))
        }

    private fun reset() {
        closing = false
        if (shown != null) {
            shown = null
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
        const val SETTINGS_FRAME_TOP = 0.19f
        const val OPEN_DURATION_MS = 500L
        const val CLOSE_DURATION_MS = 300L
    }
}
