package com.snatik.matches.ui.minigame

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.snatik.matches.R
import com.snatik.matches.game.minigame.MiniGameRules
import com.snatik.matches.game.minigame.OddOneOut
import com.snatik.matches.ui.character.CharacterDrawable
import kotlinx.coroutines.delay

/** "Odd one out": a row of the same character with one that is not; tap the odd one. */
class OddOneOutFragment : PartyGameFragment() {

    override fun plays(rules: MiniGameRules) = rules is OddOneOut

    override suspend fun play() {
        val rules = mini.rules as OddOneOut
        val cast = cast(rules.turns.flatMap { listOf(it.character, it.other) })

        if (rules.isOver) {
            scene.setParty(row(rules.turns.last(), cast))
            scene.enter()
            return
        }
        while (true) {
            val turn = rules.currentTurn ?: break
            playTurn(turn, cast)
        }
        finish()
    }

    private fun row(turn: OddOneOut.Turn, cast: Cast): List<CharacterDrawable> = List(turn.size) { position ->
        when {
            position != turn.odd -> cast.forScene(turn.character)
            turn.kind == OddOneOut.Kind.DIFFERENT -> cast.forScene(turn.other)
            else -> cast.forScene(turn.character).apply { colorFilter = ColorMatrixColorFilter(RECOLOUR) }
        }
    }

    private suspend fun playTurn(turn: OddOneOut.Turn, cast: Cast) {
        prompt(R.string.odd_prompt)
        scene.setParty(row(turn, cast))
        scene.enter()
        delay(ENTRANCE_MS)
        while (true) {
            val position = awaitSceneTap()
            when (viewModel.answerMiniGame(position)) {
                MiniGameRules.Answer.RIGHT -> {
                    scene.spotlight(turn.odd, SPOTLIGHT_MS)
                    delay(JOY_DELAY_MS)
                    scene.celebrate()
                    delay(JOY_MS)
                    break
                }
                MiniGameRules.Answer.WRONG -> scene.shakeHead(position)
                MiniGameRules.Answer.IGNORED -> break
            }
        }
        scene.hide()
        delay(LEAVE_MS)
    }

    private companion object {
        const val ENTRANCE_MS = 900L
        const val SPOTLIGHT_MS = 900L
        const val JOY_DELAY_MS = 300L
        const val LEAVE_MS = 600L

        /** The recoloured twin: colour channels rotated, so red becomes green, green blue, blue red. */
        val RECOLOUR = ColorMatrix(
            floatArrayOf(
                0f, 0f, 1f, 0f, 0f,
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
    }
}
