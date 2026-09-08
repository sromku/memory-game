package com.snatik.matches.ui.minigame

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.snatik.matches.R
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.ShadowMatch
import kotlinx.coroutines.delay

/** "Shadow match": a black shadow stands on the ground; whose is it? */
class ShadowMatchFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is ShadowMatch

    override suspend fun play() {
        val rules = mini.rules as ShadowMatch
        val cast = cast(rules.turns.flatMap { it.choices })

        if (rules.isOver) {
            scene.setParty(listOf(cast.forScene(rules.turns.last().target)))
            scene.enter()
            return
        }
        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(turn, cast)
        }
        finish()
    }

    private suspend fun playTurn(turn: ShadowMatch.Turn, cast: Cast) {
        prompt(R.string.shadow_prompt)
        val shadow = cast.forScene(turn.target).apply { colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN) }
        scene.setParty(listOf(shadow))
        scene.enter()
        delay(ENTRANCE_MS)
        showChoices(turn.choices.map { it to cast.forCard(it) })
        while (true) {
            val choice = awaitCardTap(turn.choices)
            if (viewModel.answerMiniGame(choice) == MiniGameRules.Answer.RIGHT) {
                takeCard(choice, turn.choices)
                // The shadow becomes the character.
                scene.setParty(listOf(cast.forScene(turn.target)))
                scene.enter()
                delay(JOY_MS)
                break
            }
            shakeCard(choice, turn.choices)
        }
        hideChoices()
        scene.hide()
        delay(LEAVE_MS)
    }

    private companion object {
        const val ENTRANCE_MS = 900L
        const val LEAVE_MS = 600L
    }
}
