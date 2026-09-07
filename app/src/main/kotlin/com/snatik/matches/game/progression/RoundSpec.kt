package com.snatik.matches.game.progression

import com.snatik.matches.game.Difficulty

/**
 * One round on a difficulty's road. Rounds are derived, never stored: the same (difficulty, index)
 * always describes the same round.
 */
data class RoundSpec(
    val difficulty: Difficulty,
    /** 1-based position on the road. */
    val index: Int,
) {
    init {
        require(index in 1..Road.ROUNDS_PER_DIFFICULTY) { "Round index $index is outside the road" }
    }

    /** Seconds on the clock: the difficulty's full time at round 1, tightening to [Road.LAST_ROUND_TIME_FRACTION] by the last round. */
    val timeSeconds: Int
        get() {
            val progress = (index - 1).toFloat() / (Road.ROUNDS_PER_DIFFICULTY - 1)
            val fraction = 1f - (1f - Road.LAST_ROUND_TIME_FRACTION) * progress
            return (difficulty.timeSeconds * fraction).toInt()
        }

    /** Every fifth round is reserved for a mini-game. */
    val isSpecial: Boolean get() = index % Road.SPECIAL_EVERY == 0

    /** The mini-game of a special round: the two alternate along the road. */
    val miniGame: MiniGame?
        get() = when {
            index % (2 * Road.SPECIAL_EVERY) == 0 -> MiniGame.FOLLOW_THE_SONG
            isSpecial -> MiniGame.WHO_WAS_HERE
            else -> null
        }

    val next: RoundSpec? get() = if (index < Road.ROUNDS_PER_DIFFICULTY) copy(index = index + 1) else null
}

object Road {
    const val ROUNDS_PER_DIFFICULTY = 40
    const val SPECIAL_EVERY = 5
    const val LAST_ROUND_TIME_FRACTION = 0.7f

    /** Rounds of the previous difficulty needed before the next one opens. */
    const val ROUNDS_TO_UNLOCK_NEXT = 10

    fun rounds(difficulty: Difficulty): List<RoundSpec> = List(ROUNDS_PER_DIFFICULTY) { RoundSpec(difficulty, it + 1) }
}

/** The games that special rounds play instead of a board of cards. */
enum class MiniGame { WHO_WAS_HERE, FOLLOW_THE_SONG }
