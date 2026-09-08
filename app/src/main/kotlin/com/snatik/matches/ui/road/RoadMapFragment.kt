package com.snatik.matches.ui.road

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.snatik.matches.R
import com.snatik.matches.databinding.RoadMapFragmentBinding
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.Road
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.image.Label
import com.snatik.matches.ui.image.LabeledDrawable
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.launch

/** The road of the chosen difficulty: pick a round to play, see the stars earned so far. */
class RoadMapFragment : Fragment(R.layout.road_map_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RoadMapFragmentBinding.bind(view)
        val difficulty = viewModel.selectedDifficulty.value ?: return
        val theme = viewModel.selectedTheme.value ?: return
        binding.roadMap.onRoundSelected = viewModel::selectRound
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.themePicker.setOnClickListener { viewModel.openThemePicker() }
        binding.difficultyPicker.setOnClickListener { viewModel.openDifficultyPicker() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
            binding.starsIcon.setImageDrawable(requireContext().loadDrawable(R.drawable.level_complete_star))
            binding.themePicker.setImageDrawable(themeThumb(theme))
            binding.difficultyPicker.setImageDrawable(difficultyThumb(theme, difficulty, viewModel.progress.value))
        }

        // A round finished just before this screen came back is celebrated on the map, once.
        var celebrate = viewModel.consumeFinishedRound()?.takeIf { it.theme == theme && it.difficulty == difficulty }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collect { progress ->
                    binding.roadMap.show(RoadNode.road(theme, difficulty, progress), celebrate)
                    celebrate = null
                    binding.starsText.text = getString(R.string.score_format, progress.starsOn(theme, difficulty))
                    binding.roundsText.text = getString(R.string.road_progress_format, progress.completedCount(theme, difficulty), Road.ROUNDS_PER_DIFFICULTY)
                }
            }
        }
    }

    /** The theme's card, small, as the button that changes the theme. */
    private suspend fun themeThumb(theme: GameTheme): LabeledDrawable {
        val cards = resources.obtainTypedArray(theme.cardImagesRes)
        val art = try { cards.getResourceId(viewModel.progress.value.themeStars(theme), 0) } finally { cards.recycle() }
        val name = Label(getString(theme.nameRes), x = 0.5f, y = 0.078f, height = 0.062f, maxWidth = 0.5f)
        return LabeledDrawable(requireContext(), requireContext().loadDrawable(art), listOf(name))
    }

    /** The road's button, small, as the button that changes the road. */
    private suspend fun difficultyThumb(theme: GameTheme, difficulty: Difficulty, progress: Progress): LabeledDrawable {
        val buttons = resources.obtainTypedArray(R.array.difficulty_buttons)
        val index = (difficulty.level - 1) * (GameResult.MAX_STARS + 1) + progress.averageStars(theme, difficulty)
        val art = try { buttons.getResourceId(index, 0) } finally { buttons.recycle() }
        val name = Label(getString(DIFFICULTY_NAMES[difficulty.ordinal]), x = 0.5f, y = 0.28f, height = 0.2f, maxWidth = 0.76f, uppercase = true)
        return LabeledDrawable(requireContext(), requireContext().loadDrawable(art), listOf(name))
    }

    private companion object {
        val DIFFICULTY_NAMES = listOf(
            R.string.difficulty_name_1, R.string.difficulty_name_2, R.string.difficulty_name_3,
            R.string.difficulty_name_4, R.string.difficulty_name_5, R.string.difficulty_name_6,
        )
    }
}
