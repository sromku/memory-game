package com.snatik.matches.ui.difficulty

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.launch
import com.snatik.matches.R
import com.snatik.matches.databinding.DifficultySelectFragmentBinding
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.GameViewModel

class DifficultySelectFragment : Fragment(R.layout.difficulty_select_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DifficultySelectFragmentBinding.bind(view)
        val theme = viewModel.selectedTheme.value ?: return
        val buttons = listOf(
            binding.selectDifficulty1, binding.selectDifficulty2, binding.selectDifficulty3,
            binding.selectDifficulty4, binding.selectDifficulty5, binding.selectDifficulty6,
        )
        val bestTimes = listOf(
            binding.timeDifficulty1, binding.timeDifficulty2, binding.timeDifficulty3,
            binding.timeDifficulty4, binding.timeDifficulty5, binding.timeDifficulty6,
        )
        val cells = listOf(
            binding.cellDifficulty1, binding.cellDifficulty2, binding.cellDifficulty3,
            binding.cellDifficulty4, binding.cellDifficulty5, binding.cellDifficulty6,
        )
        val buttonArt = resources.obtainTypedArray(R.array.difficulty_buttons)
        try {
            Difficulty.entries.forEachIndexed { index, difficulty ->
                bindButton(buttons[index], buttonArt, theme, difficulty)
                bindBestTime(bestTimes[index], theme, difficulty)
                fitButtonToCell(cells[index], buttons[index], bestTimes[index])
            }
        } finally {
            buttonArt.recycle()
        }
        animate(buttons)
    }

    /**
     * The button art keeps its aspect ratio, so on short screens it must give up width to fit the
     * cell's height together with the footer. Only the cell's measured height can tell us how much.
     */
    private fun fitButtonToCell(cell: View, button: ImageView, footer: TextView) {
        cell.doOnLayout {
            val gap = resources.getDimensionPixelSize(R.dimen.difficulty_best_gap)
            // The footer may already have been squeezed by an oversized button, so ask for its natural height.
            footer.measure(
                View.MeasureSpec.makeMeasureSpec(cell.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val available = cell.height - footer.measuredHeight - gap
            if (available > 0 && button.maxHeight != available) {
                button.maxHeight = available
                button.maxWidth = cell.width
                button.requestLayout()
            }
        }
    }

    private fun bindButton(
        button: ImageView,
        buttonArt: android.content.res.TypedArray,
        theme: GameTheme,
        difficulty: Difficulty,
    ) {
        val stars = viewModel.highStars(theme, difficulty)
        val index = (difficulty.level - 1) * (GameResult.MAX_STARS + 1) + stars
        val art = buttonArt.getResourceId(index, 0)
        viewLifecycleOwner.lifecycleScope.launch { button.setImageDrawable(requireContext().loadDrawable(art)) }
        button.contentDescription = getString(R.string.cd_difficulty, difficulty.level)
        button.setOnClickListener { viewModel.selectDifficulty(difficulty) }
    }

    private fun bindBestTime(label: TextView, theme: GameTheme, difficulty: Difficulty) {
        val best = viewModel.bestTimeSeconds(theme, difficulty)
        // Unplayed levels keep the label's space (so the buttons line up) but show nothing.
        label.text = getString(R.string.best_time_format, (best ?: 0) / 60, (best ?: 0) % 60)
        label.isInvisible = best == null
    }

    private fun animate(views: List<View>) {
        val animators = views.flatMap {
            listOf(
                ObjectAnimator.ofFloat(it, View.SCALE_X, 0.8f, 1f),
                ObjectAnimator.ofFloat(it, View.SCALE_Y, 0.8f, 1f),
            )
        }
        AnimatorSet().apply {
            playTogether(animators)
            duration = 500
            interpolator = BounceInterpolator()
            start()
        }
    }
}
