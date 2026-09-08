package com.snatik.matches.ui.minigame

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.PartyGameFragmentBinding
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import com.snatik.matches.ui.formatClock
import com.snatik.matches.ui.image.CenterCrop
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * What every mini-game screen shares: the party scene on the theme's ground, a prompt, a clock for
 * "look" countdowns, up to three cards to pick from, taps on cards and on characters, and the
 * characters rendered once. A game is [play], one coroutine walking its rules; see [WhoWasHereFragment].
 */
abstract class PartyGameFragment : Fragment(R.layout.party_game_fragment) {

    protected val viewModel: GameViewModel by activityViewModels()
    protected lateinit var binding: PartyGameFragmentBinding
        private set
    protected lateinit var mini: GameViewModel.MiniGame
        private set
    protected val scene: PartySceneView get() = binding.scene
    protected val theme get() = mini.theme

    private val cardTaps = Channel<Int>(Channel.BUFFERED)
    private val sceneTaps = Channel<Int>(Channel.BUFFERED)
    private val choiceViews: List<ImageView> get() = listOf(binding.choice1, binding.choice2, binding.choice3)

    /** The characters of this round, drawn once for the scene and once for the cards. */
    protected inner class Cast(private val rendered: Map<Int, Pair<RenderedCharacter, RenderedCharacter>>) {
        fun forScene(image: Int) = CharacterDrawable(rendered.getValue(image).first)
        fun forCard(image: Int) = CharacterDrawable(rendered.getValue(image).second)
    }

    /** Whether this screen plays these rules; the wrong pair sends the player back to the map. */
    protected abstract fun plays(rules: MiniGameRules): Boolean

    /** The game, from the first turn to [finish]. Runs once the screen is laid out and the ground is set. */
    protected abstract suspend fun play()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = PartyGameFragmentBinding.bind(view)
        mini = viewModel.miniGame?.takeIf { plays(it.rules) } ?: run { viewModel.backToRoadMap(); return }
        binding.backButton.setOnClickListener { viewModel.backToRoadMap() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
            binding.timeBarImage.setImageDrawable(requireContext().loadDrawable(R.drawable.time_bar))
        }
        binding.root.doOnLayout {
            viewLifecycleOwner.lifecycleScope.launch {
                val background = CenterCrop.imageSize(resources, theme.backgroundRes)
                scene.setGround(CenterCrop.y(theme.groundLine, background.width, background.height, it.width, it.height))
                play()
            }
        }
    }

    /** Rasterises [images] of the theme off the main thread, at the scene's and the cards' sizes. */
    protected suspend fun cast(images: Collection<Int>): Cast {
        val assets = requireContext().assets
        val sceneSize = (binding.root.height * SCENE_RENDER_HEIGHT).toInt()
        val cardSize = resources.getDimensionPixelSize(R.dimen.who_choice_size)
        return Cast(
            images.toSet().associateWith { image ->
                val character = withContext(Dispatchers.IO) { Character.load(assets, theme.characters[image]) }
                    ?: error("Missing character asset ${theme.characters[image]}")
                RenderedCharacter.render(character, sceneSize) to RenderedCharacter.render(character, cardSize)
            },
        )
    }

    protected fun prompt(@StringRes text: Int?) {
        binding.prompt.isInvisible = text == null
        if (text != null) binding.prompt.text = getString(text)
    }

    /** Counts [seconds] down on the clock, one per second, then hides it. */
    protected suspend fun countdown(seconds: Int) {
        for (second in seconds downTo 1) {
            binding.timeBarText.text = requireContext().formatClock(second)
            binding.timeBarImage.isInvisible = false
            binding.timeBarText.isInvisible = false
            delay(1000)
        }
        binding.timeBarImage.isInvisible = true
        binding.timeBarText.isInvisible = true
    }

    protected val progressDots: SongProgressView get() = binding.progress

    /** Shows cards for [choices] (image to drawable), popping in one after another. */
    protected fun showChoices(choices: List<Pair<Int, CharacterDrawable>>) {
        choiceViews.forEachIndexed { index, view ->
            val choice = choices.getOrNull(index)
            if (choice == null) { view.isVisible = false; return@forEachIndexed }
            val (image, drawable) = choice
            view.setImageDrawable(drawable)
            view.contentDescription = getString(R.string.cd_choice, index + 1)
            view.setOnClickListener { cardTaps.trySend(image) }
            view.isVisible = true
            view.alpha = 1f
            view.scaleX = 0f
            view.scaleY = 0f
            view.animate().scaleX(1f).scaleY(1f).setStartDelay(CARD_IN_STAGGER_MS * index).setDuration(CARD_IN_MS).start()
            drawable.start()
        }
    }

    protected fun hideChoices() {
        choiceViews.forEach { view ->
            (view.drawable as? CharacterDrawable)?.stop()
            view.setImageDrawable(null)
            view.setOnClickListener(null)
            view.isVisible = false
        }
    }

    /** The card showing [image] shrinks away and the others fade. */
    protected fun takeCard(image: Int, offered: List<Int>) {
        val taken = choiceViews[offered.indexOf(image)]
        taken.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(CARD_OUT_MS).start()
        choiceViews.filter { it !== taken && it.isVisible }.forEach { it.animate().alpha(0f).setDuration(CARD_OUT_MS).start() }
    }

    /** The card showing [image] shakes its head. */
    protected fun shakeCard(image: Int, offered: List<Int>) {
        val view = choiceViews[offered.indexOf(image)]
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, -8f, 8f, -6f, 6f, -3f, 3f, 0f).setDuration(SHAKE_MS).start()
    }

    /** The next tap on one of the cards showing [offered], ignoring stale taps. */
    protected suspend fun awaitCardTap(offered: List<Int>): Int {
        while (cardTaps.tryReceive().isSuccess) Unit
        while (true) {
            val tap = cardTaps.receive()
            if (tap in offered) return tap
        }
    }

    /** Lets the party take taps and waits for the next one, as a position. */
    protected suspend fun awaitSceneTap(): Int {
        while (sceneTaps.tryReceive().isSuccess) Unit
        scene.onGuestTapped = { sceneTaps.trySend(it) }
        try {
            return sceneTaps.receive()
        } finally {
            scene.onGuestTapped = null
        }
    }

    protected fun finish() {
        prompt(null)
        viewModel.finishMiniGame()
    }

    protected companion object {
        const val SCENE_RENDER_HEIGHT = 0.27f
        const val JOY_MS = 1500L
        const val CARD_IN_MS = 300L
        const val CARD_IN_STAGGER_MS = 100L
        const val CARD_OUT_MS = 250L
        const val SHAKE_MS = 450L
    }
}
