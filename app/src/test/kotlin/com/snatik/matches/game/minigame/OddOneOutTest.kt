package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OddOneOutTest {

    private fun round(index: Int) = RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_1, index)

    @Test
    fun `early rounds use a different character, later ones sometimes a recoloured twin`() {
        val early = (0 until 30).flatMap { OddOneOut.create(round(10), characterCount = 8, random = Random(it)).turns }
        assertTrue(early.all { it.kind == OddOneOut.Kind.DIFFERENT && it.other != it.character && it.size == 4 })
        val late = (0 until 30).flatMap { OddOneOut.create(round(60), characterCount = 8, random = Random(it)).turns }
        assertTrue(late.any { it.kind == OddOneOut.Kind.RECOLOURED && it.other == it.character })
        assertTrue(late.any { it.kind == OddOneOut.Kind.DIFFERENT })
        assertTrue(late.all { it.size == 5 && it.odd in 0 until 5 })
    }

    @Test
    fun `only the odd position is right`() {
        val game = OddOneOut.create(round(10), characterCount = 8, random = Random(3))
        val turn = game.turns[0]
        assertEquals(MiniGameRules.Answer.WRONG, game.answer((turn.odd + 1) % turn.size))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(turn.odd))
        assertEquals(1, game.turnIndex)
        assertEquals(2, game.stars)
    }
}
