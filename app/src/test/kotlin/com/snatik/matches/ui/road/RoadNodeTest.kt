package com.snatik.matches.ui.road

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.Road
import com.snatik.matches.game.progression.RoundResult
import com.snatik.matches.game.progression.RoundSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadNodeTest {

    private val T = GameTheme.ANIMALS

    private val difficulty = Difficulty.LEVEL_2

    @Test
    fun `a fresh road has the first round next and the rest locked`() {
        val road = RoadNode.road(T, difficulty, Progress.EMPTY)
        assertEquals(Road.ROUNDS_PER_DIFFICULTY, road.size)
        assertEquals(RoadNode.State.NEXT, road[0].state)
        assertTrue(road.drop(1).all { it.state == RoadNode.State.LOCKED })
        assertTrue(road[0].isPlayable)
        assertFalse(road[1].isPlayable)
    }

    @Test
    fun `played rounds are done with their stars, then comes next`() {
        val progress = Progress.EMPTY
            .record(RoundSpec(T, difficulty, 1), RoundResult(3, 20))
            .record(RoundSpec(T, difficulty, 2), RoundResult(1, 60))
        val road = RoadNode.road(T, difficulty, progress)
        assertEquals(RoadNode(RoundSpec(T, difficulty, 1), RoadNode.State.DONE, 3), road[0])
        assertEquals(RoadNode(RoundSpec(T, difficulty, 2), RoadNode.State.DONE, 1), road[1])
        assertEquals(RoadNode.State.NEXT, road[2].state)
        assertEquals(RoadNode.State.LOCKED, road[3].state)
    }

    @Test
    fun `a finished road has no next round`() {
        val progress = Road.rounds(T, difficulty).fold(Progress.EMPTY) { p, r -> p.record(r, RoundResult(2, 30)) }
        assertTrue(RoadNode.road(T, difficulty, progress).all { it.state == RoadNode.State.DONE })
    }
}
