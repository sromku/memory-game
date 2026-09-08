package com.snatik.matches.game.minigame

import com.snatik.matches.game.Board
import com.snatik.matches.game.progression.RoundSpec
import kotlin.random.Random

/**
 * Rules of "Peek and find": a board of cards is shown face up for a moment, then turned over, and
 * the player is asked for one picture at a time: "where is the pig?". Answers are tiles of the
 * board; a right tile stays open, a wrong one closes again.
 */
class PeekAndFind private constructor(val board: Board, val targets: List<Int>) : MiniGameRules {

    var targetIndex: Int = 0
        private set

    var mistakes: Int = 0
        private set

    private val found = mutableSetOf<Int>()

    /** The picture asked for now, or null when all were found. */
    val currentTarget: Int? get() = targets.getOrNull(targetIndex)

    fun isFound(tile: Int): Boolean = tile in found

    override val isOver: Boolean get() = targetIndex >= targets.size

    override val stars: Int
        get() = when {
            mistakes == 0 -> 3
            mistakes <= 2 -> 2
            else -> 1
        }

    override fun answer(choice: Int): MiniGameRules.Answer {
        val target = currentTarget ?: return MiniGameRules.Answer.IGNORED
        if (choice in found || choice !in 0 until board.tileCount) return MiniGameRules.Answer.IGNORED
        if (board.imageOf(choice) != target) {
            mistakes++
            return MiniGameRules.Answer.WRONG
        }
        found += choice
        targetIndex++
        return MiniGameRules.Answer.RIGHT
    }

    companion object {
        /** Three pictures to find, four from round 50. */
        fun targetCount(round: RoundSpec): Int = if (round.index < 50) 3 else 4

        fun create(round: RoundSpec, characterCount: Int, random: Random = Random.Default): PeekAndFind {
            val board = Board.create(round.difficulty.tileCount, (0 until characterCount).toList(), random)
            val onBoard = (0 until board.tileCount).map(board::imageOf).distinct()
            val targets = onBoard.shuffled(random).take(targetCount(round).coerceAtMost(onBoard.size))
            return PeekAndFind(board, targets)
        }
    }
}
