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
import com.snatik.matches.game.progression.Road
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.launch

/** The road of the chosen difficulty: pick a round to play, see the stars earned so far. */
class RoadMapFragment : Fragment(R.layout.road_map_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RoadMapFragmentBinding.bind(view)
        val difficulty = viewModel.selectedDifficulty.value ?: return
        binding.roadMap.onRoundSelected = viewModel::selectRound
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
            binding.starsIcon.setImageDrawable(requireContext().loadDrawable(R.drawable.level_complete_star))
        }

        // A round finished just before this screen came back is celebrated on the map, once.
        var celebrate = viewModel.consumeFinishedRound()?.takeIf { it.difficulty == difficulty }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collect { progress ->
                    binding.roadMap.show(RoadNode.road(difficulty, progress), celebrate)
                    celebrate = null
                    binding.starsText.text = getString(R.string.score_format, progress.starsOn(difficulty))
                    binding.roundsText.text = getString(R.string.road_progress_format, progress.completedCount(difficulty), Road.ROUNDS_PER_DIFFICULTY)
                }
            }
        }
    }
}
