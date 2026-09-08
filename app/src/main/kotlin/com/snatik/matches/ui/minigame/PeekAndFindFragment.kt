package com.snatik.matches.ui.minigame

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.PeekAndFindFragmentBinding
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.PeekAndFind
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import com.snatik.matches.ui.formatClock
import com.snatik.matches.ui.game.BoardView
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** "Peek and find": the board shows all its pictures for a moment, then asks for them one by one. */
class PeekAndFindFragment : Fragment(R.layout.peek_and_find_fragment) {

    private val viewModel: GameViewModel by activityViewModels()
    private val taps = Channel<Int>(Channel.BUFFERED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = PeekAndFindFragmentBinding.bind(view)
        val mini = viewModel.miniGame?.takeIf { it.rules is PeekAndFind } ?: run { viewModel.backToRoadMap(); return }
        val rules = mini.rules as PeekAndFind
        binding.backButton.setOnClickListener { viewModel.backToRoadMap() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
            binding.timeBarImage.setImageDrawable(requireContext().loadDrawable(R.drawable.time_bar))
        }
        val board = BoardView(requireContext()).apply { onTileClick = { taps.trySend(it) } }
        binding.gameContainer.addView(board, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        board.doOnLayout {
            board.post {
                board.setBoard(mini.round.difficulty, isGone = { false }, faceUp = null)
                viewLifecycleOwner.lifecycleScope.launch { play(binding, board, mini, rules) }
            }
        }
    }

    private suspend fun play(binding: PeekAndFindFragmentBinding, board: BoardView, mini: GameViewModel.MiniGame, rules: PeekAndFind) {
        val theme = mini.theme
        val assets = requireContext().assets
        val tileSize = board.tileSize
        val targetSize = resources.getDimensionPixelSize(R.dimen.peek_target_size)
        val tilesByImage = (0 until rules.board.tileCount).groupBy(rules.board::imageOf)
        val rendered = tilesByImage.keys.associateWith { image ->
            val character = withContext(Dispatchers.IO) { Character.load(assets, theme.characters[image]) }
                ?: error("Missing character asset ${theme.characters[image]}")
            RenderedCharacter.render(character, tileSize) to RenderedCharacter.render(character, targetSize)
        }
        for ((image, tiles) in tilesByImage) tiles.forEach { board.setTileCharacter(it, CharacterDrawable(rendered.getValue(image).first)) }

        if (rules.isOver) {
            (0 until rules.board.tileCount).filter(rules::isFound).forEach(board::flipUp)
            return
        }

        // Peek: every card face up for a moment.
        binding.prompt.text = getString(R.string.who_look)
        binding.prompt.isInvisible = false
        delay(DEAL_MS)
        (0 until rules.board.tileCount).forEach(board::flipUp)
        countdown(binding, PEEK_SECONDS_BASE + rules.board.tileCount / 6)
        (0 until rules.board.tileCount).forEach(board::flipDown)
        delay(CLOSE_MS)

        while (true) {
            val target = rules.currentTarget ?: break
            binding.prompt.text = getString(R.string.peek_find)
            binding.target.setImageDrawable(CharacterDrawable(rendered.getValue(target).second).also { it.start() })
            binding.target.isInvisible = false
            while (rules.currentTarget == target) {
                while (taps.tryReceive().isSuccess) Unit
                val tile = taps.receive()
                if (rules.isFound(tile)) continue
                board.flipUp(tile)
                when (viewModel.answerMiniGame(tile)) {
                    MiniGameRules.Answer.RIGHT -> {
                        board.celebrate(tile)
                        delay(FOUND_MS)
                    }
                    MiniGameRules.Answer.WRONG -> {
                        delay(WRONG_SHOW_MS)
                        board.flipDown(tile)
                        delay(CLOSE_MS)
                    }
                    MiniGameRules.Answer.IGNORED -> board.flipDown(tile)
                }
            }
        }
        binding.prompt.isInvisible = true
        binding.target.isInvisible = true
        viewModel.finishMiniGame()
    }

    private suspend fun countdown(binding: PeekAndFindFragmentBinding, seconds: Int) {
        for (second in seconds downTo 1) {
            binding.timeBarText.text = requireContext().formatClock(second)
            binding.timeBarImage.isInvisible = false
            binding.timeBarText.isInvisible = false
            delay(1000)
        }
        binding.timeBarImage.isInvisible = true
        binding.timeBarText.isInvisible = true
    }

    private companion object {
        const val DEAL_MS = 900L
        const val PEEK_SECONDS_BASE = 3
        const val CLOSE_MS = 700L
        const val FOUND_MS = 900L
        const val WRONG_SHOW_MS = 900L
    }
}
