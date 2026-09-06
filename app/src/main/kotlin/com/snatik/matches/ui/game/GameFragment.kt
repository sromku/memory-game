package com.snatik.matches.ui.game

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.snatik.matches.R
import com.snatik.matches.databinding.GameFragmentBinding
import com.snatik.matches.game.Game
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.GameViewModel.BoardEvent
import com.snatik.matches.ui.formatClock
import com.snatik.matches.ui.image.BitmapLoader
import kotlinx.coroutines.launch

class GameFragment : Fragment(R.layout.game_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = GameFragmentBinding.bind(view)
        val game = viewModel.game ?: return
        val board = BoardView(requireContext()).apply {
            onTileClick = { tile -> if (viewModel.flipTile(tile)) flipUp(tile) }
        }
        binding.gameContainer.addView(board, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        // Build the tiles once the board knows its size, but outside the layout pass that reported it,
        // since views added during layout are not measured until something else invalidates.
        board.doOnLayout {
            board.post {
                board.setBoard(game)
                loadTileImages(game, board)
            }
        }
        if (game.result != null) hideClock(binding)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.remainingSeconds.collect { binding.timeBarText.text = requireContext().formatClock(it) }
                }
                launch {
                    viewModel.boardEventFlow.collect { event ->
                        when (event) {
                            is BoardEvent.HidePair -> board.hide(event.first, event.second)
                            is BoardEvent.FlipDown -> board.flipDown(event.first, event.second)
                            is BoardEvent.Won -> hideClock(binding)
                        }
                    }
                }
            }
        }
    }

    private fun hideClock(binding: GameFragmentBinding) {
        binding.timeBarText.isVisible = false
        binding.timeBarImage.isVisible = false
    }

    /** Decodes each distinct card image once, at the size the tiles are actually shown. */
    private fun loadTileImages(game: Game, board: BoardView) {
        val size = board.tileSize
        val tilesByImage = (0 until game.board.tileCount).groupBy(game.board::imageOf)
        viewLifecycleOwner.lifecycleScope.launch {
            for ((image, tiles) in tilesByImage) {
                launch {
                    val bitmap = BitmapLoader.decodeSampled(resources, image, size, size)
                    tiles.forEach { board.setTileImage(it, bitmap) }
                }
            }
        }
    }
}
