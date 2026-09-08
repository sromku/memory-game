package com.snatik.matches.game.minigame

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatChangedTest {

    private fun round(index: Int) = RoundSpec(GameTheme.ANIMALS, Difficulty.LEVEL_2, index)

    @Test
    fun `the party grows from three to five`() {
        assertEquals(3, WhatChanged.partySize(round(5)))
        assertEquals(3, WhatChanged.partySize(round(20)))
        assertEquals(4, WhatChanged.partySize(round(25)))
        assertEquals(5, WhatChanged.partySize(round(45)))
        assertEquals(5, WhatChanged.partySize(round(100)))
    }

    @Test
    fun `every turn changes exactly one thing`() {
        repeat(60) { seed ->
            val game = WhatChanged.create(round(30), characterCount = 20, random = Random(seed))
            for (turn in game.turns) {
                val differences = turn.before.indices.filter { turn.before[it] != turn.after[it] }
                when (turn.change) {
                    WhatChanged.Change.SWAP -> {
                        assertEquals(2, differences.size)
                        assertEquals(differences.toSet(), turn.changed)
                        assertEquals(turn.before.toSet(), turn.after.toSet())
                    }
                    WhatChanged.Change.REPLACE -> {
                        assertEquals(1, differences.size)
                        assertEquals(differences.toSet(), turn.changed)
                        assertTrue(turn.after[differences[0]] !in turn.before)
                    }
                }
            }
        }
    }

    @Test
    fun `right taps move on, wrong taps cost stars, never below one`() {
        val game = WhatChanged.create(round(5), characterCount = 10, random = Random(4))
        val turn = game.turns[0]
        val wrong = turn.after.indices.first { it !in turn.changed }
        assertEquals(MiniGameRules.Answer.WRONG, game.answer(wrong))
        assertEquals(MiniGameRules.Answer.RIGHT, game.answer(turn.changed.first()))
        assertEquals(1, game.turnIndex)
        assertEquals(2, game.stars)
        repeat(5) { game.answer(game.turns[1].after.indices.first { it !in game.turns[1].changed }) }
        assertEquals(1, game.stars)
    }
}
