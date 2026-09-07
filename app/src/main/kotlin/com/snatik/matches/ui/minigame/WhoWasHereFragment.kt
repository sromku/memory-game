package com.snatik.matches.ui.minigame

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.WhoWasHereFragmentBinding
import com.snatik.matches.game.minigame.WhoWasHere
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * "Who was here?": look at the party, watch it hide, spot who did not come back. One coroutine
 * walks the rules through their turns; the scene and the cards only show what it says.
 */
class WhoWasHereFragment : Fragment(R.layout.who_was_here_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    /** Taps on the choice cards, as the image index they show. */
    private val taps = Channel<Int>(Channel.BUFFERED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = WhoWasHereFragmentBinding.bind(view)
        val mini = viewModel.miniGame ?: run { viewModel.backToRoadMap(); return }
        binding.backButton.setOnClickListener { viewModel.backToRoadMap() }
        viewLifecycleOwner.lifecycleScope.launch {
            binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back))
            binding.timeBarImage.setImageDrawable(requireContext().loadDrawable(R.drawable.time_bar))
        }
        binding.root.doOnLayout {
            viewLifecycleOwner.lifecycleScope.launch { play(binding, mini) }
        }
    }

    private suspend fun play(binding: WhoWasHereFragmentBinding, mini: GameViewModel.MiniGame) {
        val theme = mini.theme
        val rules = mini.rules
        val root = binding.root
        val choiceViews = listOf(binding.choice1, binding.choice2, binding.choice3)

        // Every character of every turn, rasterised once for the scene and once for the cards.
        val needed = rules.turns.flatMap { it.party + it.choices }.toSet()
        val assets = requireContext().assets
        val sceneSize = (root.height * SCENE_RENDER_HEIGHT).toInt()
        val cardSize = resources.getDimensionPixelSize(R.dimen.who_choice_size)
        val rendered = needed.associateWith { image ->
            val character = withContext(Dispatchers.IO) { Character.load(assets, theme.characters[image]) }
                ?: error("Missing character asset ${theme.characters[image]}")
            RenderedCharacter.render(character, sceneSize) to RenderedCharacter.render(character, cardSize)
        }
        val background = CenterCrop.imageSize(resources, theme.backgroundRes)
        binding.scene.setGround(CenterCrop.y(theme.groundLine, background.width, background.height, root.width, root.height))

        if (rules.isOver) {
            // Back after a configuration change with the round already finished: just the last party.
            val last = rules.turns.last()
            binding.scene.setParty(last.party.map { CharacterDrawable(rendered.getValue(it).first) })
            binding.scene.enter()
            return
        }

        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(binding, turn, rendered, choiceViews)
        }
        binding.prompt.isInvisible = true
        viewModel.finishMiniGame()
    }

    private suspend fun playTurn(
        binding: WhoWasHereFragmentBinding,
        turn: WhoWasHere.Turn,
        rendered: Map<Int, Pair<RenderedCharacter, RenderedCharacter>>,
        choiceViews: List<ImageView>,
    ) {
        val scene = binding.scene
        val party = turn.party.map { CharacterDrawable(rendered.getValue(it).first) }

        // Look!
        binding.prompt.text = getString(R.string.who_look)
        binding.prompt.isInvisible = false
        scene.setParty(party)
        scene.enter()
        val lookSeconds = LOOK_SECONDS_BASE + turn.party.size / 2
        for (second in lookSeconds downTo 1) {
            binding.timeBarText.text = requireContext().formatClock(second)
            setClockVisible(binding, true)
            delay(1000)
        }
        setClockVisible(binding, false)

        // Hide.
        scene.hide()
        delay(HIDE_PAUSE_MS)

        // Who is missing?
        binding.prompt.text = getString(R.string.who_missing)
        scene.enter(except = turn.missingPosition)
        delay(RETURN_PAUSE_MS)
        scene.showGap(turn.missingPosition)
        showChoices(choiceViews, turn.choices.map { it to CharacterDrawable(rendered.getValue(it).second) })

        while (true) {
            val choice = awaitTap(choiceViews, turn.choices)
            val view = choiceViews[turn.choices.indexOf(choice)]
            if (viewModel.answerWhoWasHere(choice)) {
                view.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(CARD_OUT_MS).start()
                choiceViews.filter { it !== view }.forEach { it.animate().alpha(0f).setDuration(CARD_OUT_MS).start() }
                scene.fillGap()
                delay(JOY_MS)
                break
            }
            ObjectAnimator.ofFloat(view, View.ROTATION, 0f, -8f, 8f, -6f, 6f, -3f, 3f, 0f).setDuration(SHAKE_MS).start()
        }
        hideChoices(choiceViews)
    }

    private fun showChoices(views: List<ImageView>, choices: List<Pair<Int, CharacterDrawable>>) {
        views.forEachIndexed { index, view ->
            val (image, drawable) = choices[index]
            view.setImageDrawable(drawable)
            view.contentDescription = getString(R.string.cd_choice, index + 1)
            view.setOnClickListener { taps.trySend(image) }
            view.isVisible = true
            view.alpha = 1f
            view.scaleX = 0f
            view.scaleY = 0f
            view.animate().scaleX(1f).scaleY(1f).setStartDelay(CARD_IN_STAGGER_MS * index).setDuration(CARD_IN_MS).start()
            drawable.start()
        }
    }

    private fun hideChoices(views: List<ImageView>) {
        views.forEach { view ->
            (view.drawable as? CharacterDrawable)?.stop()
            view.setImageDrawable(null)
            view.setOnClickListener(null)
            view.isVisible = false
        }
    }

    /** The next tap on one of the cards currently shown, ignoring stale taps. */
    private suspend fun awaitTap(views: List<ImageView>, offered: List<Int>): Int {
        while (taps.tryReceive().isSuccess) Unit
        return suspendCancellableCoroutine { continuation ->
            val job = viewLifecycleOwner.lifecycleScope.launch {
                for (tap in taps) if (tap in offered) { continuation.resume(tap); break }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }.also { views.forEach { it.isEnabled = true } }
    }

    private fun setClockVisible(binding: WhoWasHereFragmentBinding, visible: Boolean) {
        binding.timeBarImage.isInvisible = !visible
        binding.timeBarText.isInvisible = !visible
    }

    private companion object {
        const val SCENE_RENDER_HEIGHT = 0.27f
        const val LOOK_SECONDS_BASE = 2
        const val HIDE_PAUSE_MS = 900L
        const val RETURN_PAUSE_MS = 800L
        const val JOY_MS = 1500L
        const val CARD_IN_MS = 300L
        const val CARD_IN_STAGGER_MS = 100L
        const val CARD_OUT_MS = 250L
        const val SHAKE_MS = 450L
    }
}
