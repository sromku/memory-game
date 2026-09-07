package com.snatik.matches.ui.minigame

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.FollowTheSongFragmentBinding
import com.snatik.matches.game.minigame.FollowTheSong
import com.snatik.matches.game.minigame.FollowTheSong.Outcome
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import com.snatik.matches.ui.image.CenterCrop
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "Follow the song": the party sings, the child taps the singers in the same order, and the song
 * grows. One coroutine walks the rules; the scene shows who sings and takes the taps.
 */
class FollowTheSongFragment : Fragment(R.layout.follow_the_song_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    /** Taps on the party, as positions. */
    private val taps = Channel<Int>(Channel.BUFFERED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FollowTheSongFragmentBinding.bind(view)
        val mini = viewModel.miniGame?.takeIf { it.rules is FollowTheSong } ?: run { viewModel.backToRoadMap(); return }
        binding.backButton.setOnClickListener { viewModel.backToRoadMap() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
        }
        binding.root.doOnLayout {
            viewLifecycleOwner.lifecycleScope.launch { play(binding, mini) }
        }
    }

    private suspend fun play(binding: FollowTheSongFragmentBinding, mini: GameViewModel.MiniGame) {
        val theme = mini.theme
        val rules = mini.rules as FollowTheSong
        val root = binding.root
        val scene = binding.scene

        // The singers: a few of the theme's characters, the same ones for the whole round.
        val assets = requireContext().assets
        val size = (root.height * SCENE_RENDER_HEIGHT).toInt()
        val singers = theme.characters.indices.shuffled(kotlin.random.Random(mini.startedAtMillis)).take(rules.partySize).map { image ->
            val character = withContext(Dispatchers.IO) { Character.load(assets, theme.characters[image]) }
                ?: error("Missing character asset ${theme.characters[image]}")
            CharacterDrawable(RenderedCharacter.render(character, size))
        }
        val background = CenterCrop.imageSize(resources, theme.backgroundRes)
        scene.setGround(CenterCrop.y(theme.groundLine, background.width, background.height, root.width, root.height))
        scene.setParty(singers)
        scene.enter()
        binding.progress.set(rules.song.size, rules.length - 1)
        if (rules.isOver) return // back after a configuration change with the round already finished
        delay(ENTRANCE_MS)

        var listenAgain = false
        while (!rules.isOver) {
            // Listen!
            scene.onGuestTapped = null
            binding.prompt.text = getString(if (listenAgain) R.string.song_again else R.string.song_listen)
            binding.prompt.isInvisible = false
            binding.progress.set(rules.song.size, rules.length - 1)
            delay(PROMPT_MS)
            for (singer in rules.currentSequence) {
                scene.spotlight(singer, NOTE_MS)
                viewModel.sing(singer)
                delay(NOTE_MS + NOTE_GAP_MS)
            }

            // Your turn!
            binding.prompt.text = getString(R.string.song_your_turn)
            while (taps.tryReceive().isSuccess) Unit
            scene.onGuestTapped = { taps.trySend(it) }
            listenAgain = false
            answering@ while (true) {
                val singer = taps.receive()
                val outcome = viewModel.tapSinger(singer)
                if (outcome != Outcome.MISTAKE && outcome != Outcome.IGNORED) scene.spotlight(singer, NOTE_MS)
                when (outcome) {
                    Outcome.CONTINUE, Outcome.IGNORED -> Unit
                    Outcome.TURN_COMPLETE, Outcome.SONG_COMPLETE -> {
                        scene.onGuestTapped = null
                        binding.progress.set(rules.song.size, rules.length - 1)
                        delay(JOY_DELAY_MS)
                        for (position in 0 until rules.partySize) {
                            scene.spotlight(position, NOTE_MS)
                            delay(JOY_STAGGER_MS)
                        }
                        delay(JOY_MS)
                        break@answering
                    }
                    Outcome.MISTAKE -> {
                        scene.onGuestTapped = null
                        listenAgain = true
                        delay(MISTAKE_MS)
                        break@answering
                    }
                }
            }
        }
        binding.prompt.isInvisible = true
        viewModel.finishMiniGame()
    }

    private companion object {
        const val SCENE_RENDER_HEIGHT = 0.27f
        const val ENTRANCE_MS = 1100L
        const val PROMPT_MS = 700L
        const val NOTE_MS = 550L
        const val NOTE_GAP_MS = 200L
        const val JOY_DELAY_MS = 400L
        const val JOY_STAGGER_MS = 120L
        const val JOY_MS = 900L
        const val MISTAKE_MS = 1100L
    }
}
