package com.snatik.matches.ui.theme

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.ThemeSelectFragmentBinding
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.image.Label
import com.snatik.matches.ui.image.LabeledDrawable
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * The themes as cards in a row that scrolls: three and a bit fit on the screen, so the cut-off
 * card says there is more, chevrons at the edges say which way, and the row nudges once on arrival.
 */
class ThemeSelectFragment : Fragment(R.layout.theme_select_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ThemeSelectFragmentBinding.bind(view)
        val margin = resources.getDimensionPixelSize(R.dimen.theme_margin)
        val padding = resources.getDimensionPixelSize(R.dimen.theme_padding)
        val cardWidth = ((resources.displayMetrics.widthPixels - 2 * padding - 2 * margin * (CARDS_ON_SCREEN - 1).toInt()) / CARDS_ON_SCREEN).toInt()
        val step = cardWidth + 2 * margin
        val cards = GameTheme.entries.mapIndexed { index, theme ->
            ImageView(requireContext()).apply {
                adjustViewBounds = true
                contentDescription = getString(theme.nameRes)
                scaleX = 0f
                scaleY = 0f
                setOnClickListener { viewModel.selectTheme(theme) }
                binding.cards.addView(this, LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = if (index == 0) 0 else margin
                    marginEnd = if (index == GameTheme.entries.size - 1) 0 else margin
                })
            } to theme
        }
        // Every card appears at once, then they grow in together.
        viewLifecycleOwner.lifecycleScope.launch {
            val drawables = cards.map { (_, theme) -> async { requireContext().loadDrawable(cardArt(theme)) } }.awaitAll()
            cards.forEachIndexed { index, (card, theme) ->
                val name = Label(getString(theme.nameRes), x = 0.5f, y = 0.078f, height = 0.062f, maxWidth = 0.5f)
                card.setImageDrawable(LabeledDrawable(requireContext(), drawables[index], listOf(name)))
                animateShow(card)
            }
            binding.scroller.post { nudge(binding, step) }
        }
        binding.scroller.setOnScrollChangeListener { _, _, _, _, _ -> updateArrows(binding) }
        binding.scroller.doOnLayout { updateArrows(binding) }
        binding.scrollLeft.setOnClickListener { binding.scroller.smoothScrollBy(-step, 0) }
        binding.scrollRight.setOnClickListener { binding.scroller.smoothScrollBy(step, 0) }
        viewLifecycleOwner.lifecycleScope.launch { binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back)) }
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun cardArt(theme: GameTheme): Int {
        val cards = resources.obtainTypedArray(theme.cardImagesRes)
        try {
            return cards.getResourceId(viewModel.progress.value.themeStars(theme), 0)
        } finally {
            cards.recycle()
        }
    }

    /** Each chevron shows only while there is something beyond it. */
    private fun updateArrows(binding: ThemeSelectFragmentBinding) {
        val scroller = binding.scroller
        val max = binding.cards.width + scroller.paddingLeft + scroller.paddingRight - scroller.width
        binding.scrollLeft.isVisible = scroller.scrollX > 0
        binding.scrollRight.isVisible = scroller.scrollX < max - 1
    }

    /** A first-visit hint: the row slides a little towards the hidden cards and settles back. */
    private fun nudge(binding: ThemeSelectFragmentBinding, step: Int) {
        if (!binding.scrollRight.isVisible || binding.scroller.scrollX > 0) return
        ValueAnimator.ofInt(0, (step * NUDGE_FRACTION).toInt(), 0).apply {
            duration = NUDGE_MS
            startDelay = NUDGE_DELAY_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { binding.scroller.scrollTo(it.animatedValue as Int, 0) }
            start()
        }
    }

    private fun animateShow(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.5f, 1f)
        AnimatorSet().apply {
            duration = 300
            playTogether(scaleX, scaleY)
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    private companion object {
        const val CARDS_ON_SCREEN = 3.4f
        const val NUDGE_FRACTION = 0.3f
        const val NUDGE_MS = 900L
        const val NUDGE_DELAY_MS = 600L
    }
}
