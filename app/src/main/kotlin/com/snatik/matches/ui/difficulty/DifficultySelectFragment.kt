package com.snatik.matches.ui.difficulty

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
            val overlap = resources.getDimensionPixelSize(R.dimen.difficulty_best_overlap)
            val available = cell.height - footer.height - overlap
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
        button.setImageResource(buttonArt.getResourceId(index, 0))
        button.contentDescription = getString(R.string.cd_difficulty, difficulty.level)
        button.setOnClickListener { viewModel.selectDifficulty(difficulty) }
    }

    private fun bindBestTime(label: TextView, theme: GameTheme, difficulty: Difficulty) {
        val best = viewModel.bestTimeSeconds(theme, difficulty)
        label.text = if (best == null) {
            getString(R.string.best_time_none)
        } else {
            getString(R.string.best_time_format, (best % 3600) / 60, best % 60)
        }
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
