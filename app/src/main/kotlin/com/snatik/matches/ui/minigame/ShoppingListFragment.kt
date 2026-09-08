package com.snatik.matches.ui.minigame

import com.snatik.matches.R
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.ShoppingList
import kotlinx.coroutines.delay

/** "Shopping list": remember a few of the row, then tap them in that order. */
class ShoppingListFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is ShoppingList

    override suspend fun play() {
        val rules = mini.rules as ShoppingList
        val cast = cast(rules.turns.flatMap { it.party })

        if (rules.isOver) {
            scene.setParty(rules.turns.last().party.map(cast::forScene))
            scene.enter()
            return
        }
        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(rules, turn, cast)
        }
        finish()
    }

    private suspend fun playTurn(rules: ShoppingList, turn: ShoppingList.Turn, cast: Cast) {
        scene.setParty(turn.party.map(cast::forScene))
        scene.enter()
        delay(ENTRANCE_MS)
        progressDots.set(turn.list.size, 0)
        var again = false
        while (true) {
            // Remember!
            prompt(if (again) R.string.shop_again else R.string.shop_remember)
            val images = turn.list.map { turn.party[it] }
            showChoices(images.map { it to cast.forCard(it) })
            countdown(LOOK_SECONDS_BASE + turn.list.size)
            hideChoices()

            // Now tap them!
            prompt(R.string.shop_tap)
            var wrong = false
            while (!wrong && rules.currentTurn === turn) {
                val position = awaitSceneTap()
                when (viewModel.answerMiniGame(position)) {
                    MiniGameRules.Answer.RIGHT -> {
                        scene.spotlight(position, SPOTLIGHT_MS)
                        progressDots.set(turn.list.size, if (rules.currentTurn === turn) rules.position else turn.list.size)
                    }
                    MiniGameRules.Answer.WRONG -> {
                        scene.shakeHead(position)
                        progressDots.set(turn.list.size, 0)
                        wrong = true
                    }
                    MiniGameRules.Answer.IGNORED -> return
                }
            }
            if (!wrong) break
            again = true
            delay(MISTAKE_MS)
        }
        delay(JOY_DELAY_MS)
        scene.celebrate()
        delay(JOY_MS)
        scene.hide()
        delay(LEAVE_MS)
    }

    private companion object {
        const val ENTRANCE_MS = 1000L
        const val LOOK_SECONDS_BASE = 1
        const val SPOTLIGHT_MS = 700L
        const val MISTAKE_MS = 900L
        const val JOY_DELAY_MS = 300L
        const val LEAVE_MS = 600L
    }
}
