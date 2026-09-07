package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WhoWasHereTest {

    private val T = GameTheme.ANIMALS

    private fun round(index: Int) = RoundSpec(T, Difficulty.LEVEL_2, index)

    @Test
    fun `the party grows along the road from three to six`() {
        assertEquals(3, WhoWasHere.partySize(round(5)))
        assertEquals(3, WhoWasHere.partySize(round(10)))
        assertEquals(4, WhoWasHere.partySize(round(15)))
        assertEquals(5, WhoWasHere.partySize(round(25)))
        assertEquals(6, WhoWasHere.partySize(round(35)))
        assertEquals(6, WhoWasHere.partySize(round(40)))
    }

    @Test
    fun `every turn is a fresh party with the missing one among strangers`() {
        repeat(50) { seed ->
            val game = WhoWasHere.create(round(30), characterCount = 28, random = Random(seed))
            assertEquals(WhoWasHere.TURNS, game.turns.size)
            for (turn in game.turns) {
                assertEquals(5, turn.party.size)
                assertEquals(turn.party.size, turn.party.toSet().size)
                assertEquals(WhoWasHere.CHOICES, turn.choices.size)
                assertEquals(turn.choices.size, turn.choices.toSet().size)
                assertTrue(turn.missing in turn.party)
                assertTrue(turn.missing in turn.choices)
                assertTrue(turn.choices.filter { it != turn.missing }.none { it in turn.party })
                assertEquals(turn.party.indexOf(turn.missing), turn.missingPosition)
            }
        }
    }

    @Test
    fun `answers advance on the right card and count mistakes on the wrong one`() {
        val game = WhoWasHere.create(round(5), characterCount = 10, random = Random(1))
        val first = game.turns[0]
        val wrong = first.choices.first { it != first.missing }
        assertFalse(game.answer(wrong))
        assertEquals(0, game.turnIndex)
        assertTrue(game.answer(first.missing))
        assertEquals(1, game.turnIndex)
        assertTrue(game.answer(game.turns[1].missing))
        assertTrue(game.answer(game.turns[2].missing))
        assertTrue(game.isOver)
        assertFalse("nothing to answer after the last turn", game.answer(0))
        assertEquals(1, game.mistakes)
        assertEquals(2, game.stars)
    }

    @Test
    fun `stars never drop below one`() {
        val game = WhoWasHere.create(round(5), characterCount = 10, random = Random(2))
        val turn = game.turns[0]
        val wrong = turn.choices.first { it != turn.missing }
        assertEquals(3, game.stars)
        repeat(5) { game.answer(wrong) }
        assertEquals(1, game.stars)
    }

    @Test
    fun `a theme must have enough characters for the party and the strangers`() {
        assertThrows(IllegalArgumentException::class.java) { WhoWasHere.create(round(40), characterCount = 7) }
        WhoWasHere.create(round(40), characterCount = 8)
    }
}
