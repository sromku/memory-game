package com.snatik.matches.ui.minigame

import com.snatik.matches.R
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.WhatChanged
import kotlinx.coroutines.delay

/** "What changed?": look at the party, blink, tap who moved or who is new. */
class WhatChangedFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is WhatChanged

    override suspend fun play() {
        val rules = mini.rules as WhatChanged
        val cast = cast(rules.turns.flatMap { it.before + it.after })

        if (rules.isOver) {
            scene.setParty(rules.turns.last().after.map(cast::forScene))
            scene.enter()
            return
        }
        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(turn, cast)
        }
        finish()
    }

    private suspend fun playTurn(turn: WhatChanged.Turn, cast: Cast) {
        // Look!
        prompt(R.string.who_look)
        scene.setParty(turn.before.map(cast::forScene))
        scene.enter()
        countdown(LOOK_SECONDS_BASE + turn.before.size / 2)

        // Blink: everyone ducks into the ground and comes back, one thing different.
        prompt(null)
        scene.hide()
        delay(BLINK_MS)
        scene.setParty(turn.after.map(cast::forScene))
        scene.enter()
        delay(RETURN_PAUSE_MS)

        // What changed?
        prompt(R.string.changed_prompt)
        while (true) {
            val position = awaitSceneTap()
            when (viewModel.answerMiniGame(position)) {
                MiniGameRules.Answer.RIGHT -> {
                    for (changed in turn.changed) scene.spotlight(changed, SPOTLIGHT_MS)
                    delay(JOY_DELAY_MS)
                    scene.celebrate()
                    delay(JOY_MS)
                    break
                }
                MiniGameRules.Answer.WRONG -> scene.shakeHead(position)
                MiniGameRules.Answer.IGNORED -> break
            }
        }
    }

    private companion object {
        const val LOOK_SECONDS_BASE = 2
        const val BLINK_MS = 700L
        const val RETURN_PAUSE_MS = 900L
        const val SPOTLIGHT_MS = 900L
        const val JOY_DELAY_MS = 300L
    }
}
