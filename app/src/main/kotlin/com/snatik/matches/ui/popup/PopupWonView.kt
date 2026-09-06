package com.snatik.matches.ui.popup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.snatik.matches.R
import com.snatik.matches.databinding.PopupWonViewBinding
import com.snatik.matches.game.GameResult
import com.snatik.matches.ui.formatClock

/** "Level complete": counts the remaining time down into the score and bounces in the stars. */
@SuppressLint("ViewConstructor") // created in code only
class PopupWonView(
    context: Context,
    onBack: () -> Unit,
    onNext: () -> Unit,
) : RelativeLayout(context) {

    private val binding = PopupWonViewBinding.inflate(LayoutInflater.from(context), this)
    private val pending = mutableListOf<Runnable>()
    private var countAnimator: ValueAnimator? = null

    init {
        scaleX = 0f
        scaleY = 0f
        setBackgroundResource(R.drawable.level_complete)
        binding.buttonBack.setOnClickListener { onBack() }
        binding.buttonNext.setOnClickListener { onNext() }
    }

    fun showResult(result: GameResult, onStar: () -> Unit) {
        // The time taken is what the difficulty screen records as "best", so that is what is shown.
        binding.timeBarText.text = context.formatClock(result.passedSeconds)
        binding.scoreBarText.text = context.getString(R.string.score_format, 0)
        postAfter(REVEAL_DELAY_MS) {
            animateScore(result.score)
            animateStars(result.stars, onStar)
        }
    }

    override fun onDetachedFromWindow() {
        pending.forEach(::removeCallbacks)
        pending.clear()
        countAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    private fun animateStars(stars: Int, onStar: () -> Unit) {
        val starViews = listOf(binding.star1, binding.star2, binding.star3)
        starViews.forEachIndexed { index, star ->
            if (index < stars) {
                star.isVisible = true
                star.alpha = 0f
                animateStar(star, delay = index * STAR_INTERVAL_MS, onStar = onStar)
            } else {
                star.isVisible = false
            }
        }
    }

    private fun animateStar(star: View, delay: Long, onStar: () -> Unit) {
        val alpha = ObjectAnimator.ofFloat(star, View.ALPHA, 0f, 1f).apply { duration = 100 }
        val scaleX = ObjectAnimator.ofFloat(star, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(star, View.SCALE_Y, 0f, 1f)
        AnimatorSet().apply {
            playTogether(alpha, scaleX, scaleY)
            interpolator = BounceInterpolator()
            startDelay = delay
            duration = 600
            start()
        }
        postAfter(delay, onStar)
    }

    private fun animateScore(score: Int) {
        countAnimator = ValueAnimator.ofInt(0, score).apply {
            duration = COUNT_DURATION_MS
            addUpdateListener { animator ->
                binding.scoreBarText.text = context.getString(R.string.score_format, animator.animatedValue as Int)
            }
            start()
        }
    }

    private fun postAfter(delayMillis: Long, action: () -> Unit) {
        val runnable = Runnable {
            action()
        }
        pending += runnable
        postDelayed(runnable, delayMillis)
    }

    private companion object {
        const val REVEAL_DELAY_MS = 500L
        const val STAR_INTERVAL_MS = 600L
        const val COUNT_DURATION_MS = 1200L
    }
}
