package com.snatik.matches.ui.road

import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.progression.Progress
import com.snatik.matches.game.progression.Road
import com.snatik.matches.game.progression.RoundSpec

/** One round of a road as the map shows it. */
data class RoadNode(val round: RoundSpec, val state: State, val stars: Int) {

    enum class State {
        /** Played; shows its stars and can be replayed. */
        DONE,
        /** The round to play now. */
        NEXT,
        /** Not reachable yet. */
        LOCKED,
    }

    val isPlayable: Boolean get() = state != State.LOCKED

    companion object {
        /** The road of [difficulty] as the player sees it, in round order. */
        fun road(difficulty: Difficulty, progress: Progress): List<RoadNode> {
            val next = progress.nextRound(difficulty)
            return Road.rounds(difficulty).map { round ->
                val result = progress.resultOf(round)
                val state = when {
                    result != null -> State.DONE
                    round == next -> State.NEXT
                    else -> State.LOCKED
                }
                RoadNode(round, state, result?.stars ?: 0)
            }
        }
    }
}
