package com.snatik.matches.ui.difficulty

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.TypedArray
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.DifficultySelectFragmentBinding
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.Road
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.image.Label
import com.snatik.matches.ui.image.LabeledDrawable
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/** The six roads: each button shows the road's stars and how far along it the player is. */
class DifficultySelectFragment : Fragment(R.layout.difficulty_select_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    /** The views of one difficulty's cell. */
    private class Cell(val cell: View, val holder: View, val button: ImageView, val lock: View, val footer: TextView)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DifficultySelectFragmentBinding.bind(view)
        val cells = with(binding) {
            listOf(
                Cell(cellDifficulty1, holderDifficulty1, selectDifficulty1, lockDifficulty1, progressDifficulty1),
                Cell(cellDifficulty2, holderDifficulty2, selectDifficulty2, lockDifficulty2, progressDifficulty2),
                Cell(cellDifficulty3, holderDifficulty3, selectDifficulty3, lockDifficulty3, progressDifficulty3),
                Cell(cellDifficulty4, holderDifficulty4, selectDifficulty4, lockDifficulty4, progressDifficulty4),
                Cell(cellDifficulty5, holderDifficulty5, selectDifficulty5, lockDifficulty5, progressDifficulty5),
                Cell(cellDifficulty6, holderDifficulty6, selectDifficulty6, lockDifficulty6, progressDifficulty6),
            )
        }
        val progress = viewModel.progress.value
        val buttonArt = resources.obtainTypedArray(R.array.difficulty_buttons)
        val arts = try {
            Difficulty.entries.mapIndexed { index, difficulty ->
                fitButtonToCell(cells[index])
                bind(cells[index], buttonArt, difficulty, progress)
            }
        } finally {
            buttonArt.recycle()
        }
        // All six buttons appear together, then bounce in as one.
        viewLifecycleOwner.lifecycleScope.launch {
            val drawables = arts.map { async { requireContext().loadDrawable(it) } }.awaitAll()
            cells.forEachIndexed { index, cell ->
                val name = Label(getString(DIFFICULTY_NAMES[index]), x = 0.5f, y = 0.28f, height = 0.2f, maxWidth = 0.76f, uppercase = true)
                cell.button.setImageDrawable(LabeledDrawable(requireContext(), drawables[index], listOf(name)))
            }
            animate(cells.map { it.holder })
        }
        viewLifecycleOwner.lifecycleScope.launch { binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back)) }
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    /** Binds everything but the button's picture, and returns the picture to load. */
    private fun bind(cell: Cell, buttonArt: TypedArray, difficulty: Difficulty, progress: Progress): Int {
        val unlocked = progress.isUnlocked(difficulty)
        val stars = if (unlocked) progress.averageStars(difficulty) else 0
        val artIndex = (difficulty.level - 1) * (GameResult.MAX_STARS + 1) + stars
        val art = buttonArt.getResourceId(artIndex, 0)
        cell.holder.scaleX = 0.8f
        cell.holder.scaleY = 0.8f
        cell.button.contentDescription = getString(R.string.cd_difficulty, difficulty.level)
        cell.lock.isVisible = !unlocked
        cell.button.colorFilter = if (unlocked) null else GREY
        cell.button.alpha = if (unlocked) 1f else LOCKED_ALPHA
        // Locked roads keep the footer's space so the buttons line up, but say nothing.
        cell.footer.text = getString(R.string.road_progress_format, progress.completedCount(difficulty), Road.ROUNDS_PER_DIFFICULTY)
        cell.footer.isInvisible = !unlocked
        cell.button.setOnClickListener {
            if (unlocked) viewModel.selectDifficulty(difficulty) else shakeHead(cell.holder)
        }
        return art
    }

    /**
     * The button art keeps its aspect ratio, so on short screens it must give up width to fit the
     * cell's height together with the footer. Only the cell's measured height can tell us how much.
     */
    private fun fitButtonToCell(cell: Cell) {
        cell.cell.doOnLayout {
            val gap = resources.getDimensionPixelSize(R.dimen.difficulty_best_gap)
            // The footer may already have been squeezed by an oversized button, so ask for its natural height.
            cell.footer.measure(
                View.MeasureSpec.makeMeasureSpec(cell.cell.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val available = cell.cell.height - cell.footer.measuredHeight - gap
            if (available > 0 && cell.button.maxHeight != available) {
                cell.button.maxHeight = available
                cell.button.maxWidth = cell.cell.width
                cell.button.requestLayout()
            }
        }
    }

    private fun shakeHead(view: View) {
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, -8f, 8f, -6f, 6f, -3f, 3f, 0f).apply {
            duration = SHAKE_MS
            start()
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

    private companion object {
        val DIFFICULTY_NAMES = listOf(
            R.string.difficulty_name_1, R.string.difficulty_name_2, R.string.difficulty_name_3,
            R.string.difficulty_name_4, R.string.difficulty_name_5, R.string.difficulty_name_6,
        )
        const val LOCKED_ALPHA = 0.55f
        const val SHAKE_MS = 450L
        val GREY = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
}
