package com.snatik.matches.game.minigame

import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "What changed?": a party stands still, the screen blinks, and one thing is different:
 * two characters swapped places, or one was replaced by a stranger. The player taps who changed.
 * Characters are the theme's image indices; answers are positions in the party, left to right.
 */
class WhatChanged private constructor(val turns: List<Turn>) : MiniGameRules {

    enum class Change { SWAP, REPLACE }

    /** [changed] holds the positions that count as right: both of a swap, the one of a replacement. */
    class Turn(val before: List<Int>, val after: List<Int>, val change: Change, val changed: Set<Int>) {
        init {
            require(before.size == after.size) { "the party keeps its size" }
            require(changed.isNotEmpty() && changed.all { it in after.indices }) { "the change is somewhere in the party" }
        }
    }

    var turnIndex: Int = 0
        private set

    var mistakes: Int = 0
        private set

    val currentTurn: Turn? get() = turns.getOrNull(turnIndex)

    override val isOver: Boolean get() = turnIndex >= turns.size

    override val stars: Int get() = (3 - mistakes).coerceAtLeast(1)

    override fun answer(choice: Int): MiniGameRules.Answer {
        val turn = currentTurn ?: return MiniGameRules.Answer.IGNORED
        if (choice !in turn.changed) {
            mistakes++
            return MiniGameRules.Answer.WRONG
        }
        turnIndex++
        return MiniGameRules.Answer.RIGHT
    }

    companion object {
        const val TURNS = 3
        const val MIN_PARTY = 3
        const val MAX_PARTY = 5

        /** Three characters at first, one more every twenty rounds, up to five. */
        fun partySize(round: RoundSpec): Int = (MIN_PARTY + (round.index - 1) / 20).coerceAtMost(MAX_PARTY)

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): WhatChanged {
            val size = partySize(round)
            require(characterCount > size) { "a stranger is needed for a replacement" }
            val turns = List(TURNS) {
                val order = (0 until characterCount).shuffled(random)
                val before = order.take(size)
                if (random.nextBoolean()) {
                    val first = random.nextInt(size)
                    val second = (first + 1 + random.nextInt(size - 1)) % size
                    val after = before.toMutableList().also { it[first] = before[second]; it[second] = before[first] }
                    Turn(before, after, Change.SWAP, setOf(first, second))
                } else {
                    val position = random.nextInt(size)
                    val after = before.toMutableList().also { it[position] = order[size] }
                    Turn(before, after, Change.REPLACE, setOf(position))
                }
            }
            return WhatChanged(turns)
        }
    }
}
