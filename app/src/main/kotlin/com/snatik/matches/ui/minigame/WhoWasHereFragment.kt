package com.snatik.matches.ui.minigame

import com.snatik.matches.R
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.WhoWasHere
import kotlinx.coroutines.delay

/** "Who was here?": look at the party, watch it hide, spot who did not come back. */
class WhoWasHereFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is WhoWasHere

    override suspend fun play() {
        val rules = mini.rules as WhoWasHere
        val cast = cast(rules.turns.flatMap { it.party + it.choices })

        if (rules.isOver) {
            // Back after a configuration change with the round already finished: just the last party.
            scene.setParty(rules.turns.last().party.map(cast::forScene))
            scene.enter()
            return
        }
        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(turn, cast)
        }
        finish()
    }

    private suspend fun playTurn(turn: WhoWasHere.Turn, cast: Cast) {
        // Look!
        prompt(R.string.who_look)
        scene.setParty(turn.party.map(cast::forScene))
        scene.enter()
        countdown(LOOK_SECONDS_BASE + turn.party.size / 2)

        // Hide.
        scene.hide()
        delay(HIDE_PAUSE_MS)

        // Who is missing?
        prompt(R.string.who_missing)
        scene.enter(except = turn.missingPosition)
        delay(RETURN_PAUSE_MS)
        scene.showGap(turn.missingPosition)
        showChoices(turn.choices.map { it to cast.forCard(it) })
        while (true) {
            val choice = awaitCardTap(turn.choices)
            if (viewModel.answerMiniGame(choice) == MiniGameRules.Answer.RIGHT) {
                takeCard(choice, turn.choices)
                scene.fillGap()
                delay(JOY_MS)
                break
            }
            shakeCard(choice, turn.choices)
        }
        hideChoices()
    }

    private companion object {
        const val LOOK_SECONDS_BASE = 2
        const val HIDE_PAUSE_MS = 900L
        const val RETURN_PAUSE_MS = 800L
    }
}
