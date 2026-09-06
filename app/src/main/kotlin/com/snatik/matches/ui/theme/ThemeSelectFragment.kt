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
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.launch
import com.snatik.matches.R
import com.snatik.matches.databinding.ThemeSelectFragmentBinding
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.GameViewModel

class ThemeSelectFragment : Fragment(R.layout.theme_select_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ThemeSelectFragmentBinding.bind(view)
        bindCard(binding.themeAnimals, GameTheme.ANIMALS)
        bindCard(binding.themeMonsters, GameTheme.MONSTERS)
        bindCard(binding.themeEmoji, GameTheme.EMOJI)
    }

    override fun onStart() {
        super.onStart()
        // Also reached by going back from the difficulty screen; the background returns to default.
        viewModel.clearTheme()
    }

    private fun bindCard(card: ImageView, theme: GameTheme) {
        val cards = resources.obtainTypedArray(theme.cardImagesRes)
        val art = try {
            cards.getResourceId(viewModel.averageStars(theme), 0)
        } finally {
            cards.recycle()
        }
        card.setOnClickListener { viewModel.selectTheme(theme) }
        viewLifecycleOwner.lifecycleScope.launch {
            card.setImageDrawable(requireContext().loadDrawable(art))
            animateShow(card)
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
}
