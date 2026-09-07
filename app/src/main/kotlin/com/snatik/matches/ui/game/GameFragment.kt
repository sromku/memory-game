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
import com.snatik.matches.ui.image.CenterCrop
import com.snatik.matches.ui.image.loadDrawable
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameFragment : Fragment(R.layout.game_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    /** The round's characters by board image, kept for the celebration at the end. */
    private val characters = mutableMapOf<Int, Character>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = GameFragmentBinding.bind(view)
        val game = viewModel.game ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            binding.timeBarImage.setImageDrawable(requireContext().loadDrawable(R.drawable.time_bar))
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
        }
        binding.backButton.setOnClickListener { viewModel.backToRoadMap() }
        binding.roundLabel.text = getString(R.string.round_format, game.round.index)
        val board = BoardView(requireContext()).apply {
            onTileClick = { tile -> if (viewModel.flipTile(tile)) flipUp(tile) }
        }
        binding.gameContainer.addView(board, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        // Build the tiles once the board knows its size, but outside the layout pass that reported it,
        // since views added during layout are not measured until something else invalidates.
        board.doOnLayout {
            board.post {
                board.setBoard(game)
                val loading = loadTileImages(game, board)
                if (game.result != null) {
                    // Back after a configuration change: the guests are already there, without confetti.
                    viewLifecycleOwner.lifecycleScope.launch {
                        loading.join()
                        celebrate(binding, game, confetti = false)
                    }
                }
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
                            is BoardEvent.Matched -> board.celebrate(event.first, event.second)
                            is BoardEvent.HidePair -> board.hide(event.first, event.second)
                            is BoardEvent.FlipDown -> board.flipDown(event.first, event.second)
                            is BoardEvent.Won -> {
                                hideClock(binding)
                                celebrate(binding, game, confetti = true)
                            }
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

    /**
     * Loads and rasterises each distinct character once, at the size the cards are shown, off the
     * main thread; both cards of a pair share the bitmaps but animate on their own.
     */
    private fun loadTileImages(game: Game, board: BoardView): Job {
        val tilesByImage = (0 until game.board.tileCount).groupBy(game.board::imageOf)
        val assets = requireContext().assets
        val size = board.tileSize
        return viewLifecycleOwner.lifecycleScope.launch {
            for ((image, tiles) in tilesByImage) {
                launch {
                    val name = game.theme.characters[image]
                    val character = withContext(Dispatchers.IO) { Character.load(assets, name) }
                        ?: error("Missing character asset $name")
                    characters[image] = character
                    val rendered = RenderedCharacter.render(character, size)
                    tiles.forEach { board.setTileCharacter(it, CharacterDrawable(rendered)) }
                }
            }
        }
    }

    /**
     * A few of the round's characters come up onto the ground of the background and hop about
     * under the popup; the first time, with confetti.
     */
    private fun celebrate(binding: GameFragmentBinding, game: Game, confetti: Boolean) {
        val root = binding.root
        viewLifecycleOwner.lifecycleScope.launch {
            val picked = characters.values.shuffled().take(GUEST_COUNT)
            if (picked.isEmpty()) return@launch
            val size = (root.height * GUEST_RENDER_HEIGHT).toInt()
            val guests = picked.map { CharacterDrawable(RenderedCharacter.render(it, size)) }
            val background = CenterCrop.imageSize(resources, game.theme.backgroundRes)
            val groundY = CenterCrop.y(game.theme.groundLine, background.width, background.height, root.width, root.height)
            val celebration = CelebrationView(requireContext())
            root.addView(celebration, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            celebration.doOnLayout { celebration.start(guests, groundY, confetti) }
        }
    }

    private companion object {
        const val GUEST_COUNT = 3
        /** Guests are rasterised at this fraction of the screen height. */
        const val GUEST_RENDER_HEIGHT = 0.26f
    }
}
