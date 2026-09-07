package com.snatik.matches.ui.theme

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.ui.image.Label
import com.snatik.matches.ui.image.LabeledDrawable
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import com.snatik.matches.R
import com.snatik.matches.databinding.ThemeSelectFragmentBinding
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.GameViewModel

class ThemeSelectFragment : Fragment(R.layout.theme_select_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ThemeSelectFragmentBinding.bind(view)
        val cards = listOf(binding.themeAnimals to GameTheme.ANIMALS, binding.themeMonsters to GameTheme.MONSTERS, binding.themeEmoji to GameTheme.EMOJI)
        val arts = cards.map { (card, theme) -> bindCard(card, theme) }
        // The three cards appear together, then grow in as one.
        viewLifecycleOwner.lifecycleScope.launch {
            val drawables = arts.map { async { requireContext().loadDrawable(it) } }.awaitAll()
            cards.forEachIndexed { index, (card, theme) ->
                val name = Label(getString(THEME_NAMES.getValue(theme)), x = 0.5f, y = 0.078f, height = 0.062f, maxWidth = 0.5f)
                card.setImageDrawable(LabeledDrawable(requireContext(), drawables[index], listOf(name)))
                animateShow(card)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch { binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back)) }
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    override fun onStart() {
        super.onStart()
        // Also reached by going back from the difficulty screen; the background returns to default.
        viewModel.clearTheme()
    }

    /** Wires the card and returns the picture it should show. */
    private fun bindCard(card: ImageView, theme: GameTheme): Int {
        val cards = resources.obtainTypedArray(theme.cardImagesRes)
        val art = try {
            cards.getResourceId(viewModel.averageStars(theme), 0)
        } finally {
            cards.recycle()
        }
        card.scaleX = 0f
        card.scaleY = 0f
        card.setOnClickListener { viewModel.selectTheme(theme) }
        return art
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
        val THEME_NAMES = mapOf(
            GameTheme.ANIMALS to R.string.theme_animals,
            GameTheme.MONSTERS to R.string.theme_monsters,
            GameTheme.EMOJI to R.string.theme_emoji,
        )
    }
}
